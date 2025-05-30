package com.configcat

import com.configcat.override.OverrideBehavior
import com.configcat.override.OverrideDataSource
import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.Reason
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.exceptions.ErrorCode
import java.time.Instant
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProviderTests {
    @Test
    fun testMetadata() {
        val provider =
            ConfigCatProvider("localhost") {
                flagOverrides = {
                    behavior = OverrideBehavior.LOCAL_ONLY
                    dataSource = OverrideDataSource.map(mapOf())
                }
            }
        assertEquals("ConfigCatProvider", provider.metadata.name)

        provider.shutdown()
    }

    @Test
    fun testMetadataOptions() {
        val options = ConfigCatOptions()
        options.flagOverrides = {
            behavior = OverrideBehavior.LOCAL_ONLY
            dataSource = OverrideDataSource.map(mapOf())
        }
        val provider = ConfigCatProvider("localhost", options)
        assertEquals("ConfigCatProvider", provider.metadata.name)

        provider.shutdown()
    }

    @Test
    fun testEval() {
        val provider =
            ConfigCatProvider("localhost") {
                flagOverrides = {
                    behavior = OverrideBehavior.LOCAL_ONLY
                    dataSource = ClassPathResourceOverrideDataSource("test_json_complex.json")
                }
            }

        val boolVal = provider.getBooleanEvaluation("enabledFeature", false, null)
        assertTrue(boolVal.value)
        assertEquals("v-enabled", boolVal.variant)
        assertEquals(Reason.DEFAULT.name, boolVal.reason)

        val intVal = provider.getIntegerEvaluation("intSetting", 0, null)
        assertEquals(5, intVal.value)
        assertEquals("v-int", intVal.variant)
        assertEquals(Reason.DEFAULT.name, intVal.reason)

        val doubleVal = provider.getDoubleEvaluation("doubleSetting", 0.0, null)
        assertEquals(1.2, doubleVal.value)
        assertEquals("v-double", doubleVal.variant)
        assertEquals(Reason.DEFAULT.name, doubleVal.reason)

        val stringVal = provider.getStringEvaluation("stringSetting", "", null)
        assertEquals("test", stringVal.value)
        assertEquals("v-string", stringVal.variant)
        assertEquals(Reason.DEFAULT.name, stringVal.reason)

        val objVal = provider.getObjectEvaluation("objectSetting", Value.Null, null)
        assertTrue(objVal.value.asStructure()!!.getValue("bool_field").asBoolean()!!)
        assertEquals("value", objVal.value.asStructure()!!.getValue("text_field").asString()!!)
        assertEquals("v-object", objVal.variant)
        assertEquals(Reason.DEFAULT.name, objVal.reason)

        provider.shutdown()
    }

    @Test
    fun testUser() {
        val ctxCustom =
            ImmutableContext(
                targetingKey = "example@matching.com",
                attributes =
                    mapOf(
                        "custom1" to Value.String("something"),
                        "custom2" to Value.Boolean(true),
                        "custom3" to Value.Integer(5),
                        "custom4" to Value.Double(1.2),
                        "custom5" to Value.List(listOf(Value.Integer(1), Value.Integer(2))),
                        "custom6" to Value.Date(Date.from(Instant.parse("2025-05-30T10:15:30.00Z"))),
                    ),
            )

        val user = ctxCustom.toConfigCatUser()
        assertEquals("example@matching.com", user.identifier)
        assertEquals("something", user.attributeFor("custom1"))
        assertEquals(true, user.attributeFor("custom2"))
        assertEquals(5, user.attributeFor("custom3"))
        assertEquals(1.2, user.attributeFor("custom4"))
        assertEquals(listOf(1, 2), user.attributeFor("custom5"))
        assertEquals(1748600130L, user.attributeFor("custom6"))
    }

    @Test
    fun testTargeting() {
        val provider =
            ConfigCatProvider("localhost") {
                flagOverrides = {
                    behavior = OverrideBehavior.LOCAL_ONLY
                    dataSource = ClassPathResourceOverrideDataSource("test_json_complex.json")
                }
            }

        val ctx =
            ImmutableContext(
                targetingKey = "example@matching.com",
            )

        val targetingResult = provider.getBooleanEvaluation("disabledFeature", false, ctx)
        assertTrue(targetingResult.value)
        assertEquals("v-disabled-t", targetingResult.variant)
        assertEquals(Reason.TARGETING_MATCH.name, targetingResult.reason)

        val ctxCustom =
            ImmutableContext(
                targetingKey = "example@matching.com",
                attributes = mapOf("custom-anything" to Value.String("something")),
            )

        val customTargetingResult = provider.getBooleanEvaluation("disabledFeature", false, ctxCustom)
        assertTrue(customTargetingResult.value)
        assertEquals("v-disabled-t", customTargetingResult.variant)
        assertEquals(Reason.TARGETING_MATCH.name, customTargetingResult.reason)

        provider.shutdown()
    }

    @Test
    fun testErrors() {
        val provider =
            ConfigCatProvider("localhost") {
                flagOverrides = {
                    behavior = OverrideBehavior.LOCAL_ONLY
                    dataSource = ClassPathResourceOverrideDataSource("test_json_complex.json")
                }
            }

        val boolVal = provider.getBooleanEvaluation("non-existing", false, null)
        assertFalse(boolVal.value)
        assertEquals(ErrorCode.FLAG_NOT_FOUND, boolVal.errorCode)
        assertEquals(Reason.ERROR.name, boolVal.reason)
        assertContains(boolVal.errorMessage!!, "Failed to evaluate setting 'non-existing' (the key was not found in config JSON)")

        val mismatchResult = provider.getBooleanEvaluation("stringSetting", false, null)
        assertFalse(mismatchResult.value)
        assertEquals(ErrorCode.TYPE_MISMATCH, mismatchResult.errorCode)
        assertEquals(Reason.ERROR.name, mismatchResult.reason)

        provider.shutdown()
    }
}
