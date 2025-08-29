package com.configcat

import dev.openfeature.kotlin.sdk.ProviderMetadata

class ConfigCatProviderMetadata : ProviderMetadata {
    override val name: String
        get() = "ConfigCatProvider"
}
