package com.configcat

import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.FeatureProvider
import dev.openfeature.sdk.Hook
import dev.openfeature.sdk.ProviderEvaluation
import dev.openfeature.sdk.ProviderMetadata
import dev.openfeature.sdk.Reason
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.exceptions.ErrorCode
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import java.util.Date
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.div

/**
 * Describes the ConfigCat OpenFeature provider.
 */
class ConfigCatProvider(
    sdkKey: String,
    options: ConfigCatOptions,
) : FeatureProvider {
    constructor(sdkKey: String, block: ConfigCatOptions.() -> Unit = {}) : this(sdkKey, ConfigCatOptions().apply(block))

    override var hooks: List<Hook<*>> = listOf()
    override val metadata: ProviderMetadata = ConfigCatProviderMetadata()

    private val snapshot: AtomicRef<ConfigCatClientSnapshot?> = atomic(null)
    private val user: AtomicRef<ConfigCatUser?> = atomic(null)
    private val client: ConfigCatClient

    init {
        options.hooks.addOnConfigChanged {
            val sn = client.snapshot()
            snapshot.value = sn
        }
        client = ConfigCatClient(sdkKey, options)
    }

    override suspend fun initialize(initialContext: EvaluationContext?) {
        val initialUser = initialContext?.toConfigCatUser()
        user.value = initialUser
        client.waitForReady()
    }

    override suspend fun onContextSet(
        oldContext: EvaluationContext?,
        newContext: EvaluationContext,
    ) {
        val newUser = newContext.toConfigCatUser()
        user.value = newUser
    }

    override fun getBooleanEvaluation(
        key: String,
        defaultValue: Boolean,
        context: EvaluationContext?,
    ): ProviderEvaluation<Boolean> {
        return eval(key, defaultValue, context)
    }

    override fun getDoubleEvaluation(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?,
    ): ProviderEvaluation<Double> {
        return eval(key, defaultValue, context)
    }

    override fun getIntegerEvaluation(
        key: String,
        defaultValue: Int,
        context: EvaluationContext?,
    ): ProviderEvaluation<Int> {
        return eval(key, defaultValue, context)
    }

    override fun getStringEvaluation(
        key: String,
        defaultValue: String,
        context: EvaluationContext?,
    ): ProviderEvaluation<String> {
        return eval(key, defaultValue, context)
    }

    override fun getObjectEvaluation(
        key: String,
        defaultValue: Value,
        context: EvaluationContext?,
    ): ProviderEvaluation<Value> {
        val stringResult = eval(key, "", context)
        if (stringResult.value.isEmpty()) {
            return ProviderEvaluation(
                defaultValue,
                reason = stringResult.reason,
                errorCode = stringResult.errorCode,
                errorMessage = stringResult.errorMessage,
            )
        }
        try {
            val json = Json.decodeFromString<JsonElement>(stringResult.value)
            return stringResult.withValue(json.toValue())
        } catch (e: Exception) {
            return ProviderEvaluation(
                defaultValue,
                reason = Reason.ERROR.name,
                errorCode = ErrorCode.TYPE_MISMATCH,
                errorMessage = "Could not parse '${stringResult.value}' as JSON (${e.message})",
            )
        }
    }

    override fun shutdown() {
        client.close()
    }

    private inline fun <reified T> eval(
        key: String,
        defaultValue: T,
        context: EvaluationContext?,
    ): ProviderEvaluation<T> {
        val snapshot = this.snapshot.value ?: client.snapshot()
        val user = this.user.value ?: context?.toConfigCatUser()
        val details = snapshot.getValueDetails(key, defaultValue, user)
        return details.toProviderEvaluation()
    }

    private fun <T, K> ProviderEvaluation<K>.withValue(value: T): ProviderEvaluation<T> =
        ProviderEvaluation(
            value,
            this.variant,
            this.reason,
            this.errorCode,
            this.errorMessage,
            this.metadata,
        )

    private fun <T> TypedEvaluationDetails<T>.toProviderEvaluation(): ProviderEvaluation<T> =
        ProviderEvaluation(
            this.value,
            this.variationId,
            this.toReason().name,
            this.toErrorCode(),
            this.error,
        )

    private fun <T> TypedEvaluationDetails<T>.toReason(): Reason {
        if (this.errorCode != EvaluationErrorCode.NONE) {
            return Reason.ERROR
        }

        if (this.matchedTargetingRule != null || this.matchedPercentageOption != null) {
            return Reason.TARGETING_MATCH
        }
        return Reason.DEFAULT
    }

    private fun <T> TypedEvaluationDetails<T>.toErrorCode(): ErrorCode? =
        when (this.errorCode) {
            EvaluationErrorCode.UNEXPECTED_ERROR -> ErrorCode.GENERAL
            EvaluationErrorCode.NONE -> null
            EvaluationErrorCode.INVALID_CONFIG_MODEL -> ErrorCode.PARSE_ERROR
            EvaluationErrorCode.SETTING_VALUE_TYPE_MISMATCH -> ErrorCode.TYPE_MISMATCH
            EvaluationErrorCode.CONFIG_JSON_NOT_AVAILABLE -> ErrorCode.PARSE_ERROR
            EvaluationErrorCode.SETTING_KEY_MISSING -> ErrorCode.FLAG_NOT_FOUND
        }

    private fun JsonElement.toValue(): Value =
        when (this) {
            is JsonNull -> Value.Null
            is JsonObject -> Value.Structure(this.map { (k, v) -> k to v.toValue() }.toMap())
            is JsonArray -> Value.List(this.map { it.toValue() })
            is JsonPrimitive -> this.toValue()
        }

    private fun JsonPrimitive.toValue(): Value =
        when {
            this.isString -> Value.String(this.contentOrNull ?: "")
            this.booleanOrNull != null -> Value.Boolean(this.boolean)
            this.intOrNull != null -> Value.Integer(this.int)
            this.floatOrNull != null -> Value.Double(this.float.toDouble())
            this.doubleOrNull != null -> Value.Double(this.double)
            else -> Value.Null
        }
}

internal fun EvaluationContext.toConfigCatUser(): ConfigCatUser {
    return ConfigCatUser(
        identifier = this.getTargetingKey(),
        email = this.getValue("Email")?.asString(),
        country = this.getValue("Country")?.asString(),
        custom =
            this.asObjectMap().filter { (_, v) -> v != null }.mapValues {
                val v = it.value
                if (v is Date) {
                    return@mapValues v.time / 1000
                }
                v as Any
            },
    )
}
