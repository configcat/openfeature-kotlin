package com.configcat

import dev.openfeature.sdk.ProviderMetadata

class ConfigCatProviderMetadata : ProviderMetadata {
    override val name: String?
        get() = "ConfigCatProvider"
}
