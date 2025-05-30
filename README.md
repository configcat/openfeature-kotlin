# ConfigCat OpenFeature Provider for Kotlin

[![CI](https://github.com/configcat/openfeature-kotlin/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/configcat/openfeature-kotlin/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.configcat/configcat-openfeature-provider?label=maven%20central)](https://search.maven.org/artifact/com.configcat/configcat-openfeature-provider/)

This repository contains an OpenFeature provider that allows [ConfigCat](https://configcat.com) to be used with
the [OpenFeature Kotlin SDK](https://github.com/open-feature/kotlin-sdk).

## Installation

```kotlin
dependencies {
    implementation("com.configcat:configcat-openfeature-provider:$providerVersion")
}
```

## Usage

The `ConfigCatProvider` function takes the SDK key and an optional `ConfigCatOptions`
argument containing the additional configuration options for
the [ConfigCat Kotlin SDK](https://github.com/configcat/kotlin-sdk):

```kotlin
coroutineScope.launch(Dispatchers.IO) {
    // Configure the provider.
    val provider = ConfigCatProvider("<YOUR-CONFIGCAT-SDK-KEY>") {
        pollingMode = autoPoll { pollingInterval = 60.seconds }
    }

    // Configure the OpenFeature API with the ConfigCat provider.
    OpenFeatureAPI.setProviderAndWait(provider)

    // Create a client.
    val client = OpenFeatureAPI.getClient()

    // Evaluate feature flag.
    val isAwesomeFeatureEnabled = client.getBooleanDetails("isAwesomeFeatureEnabled", false)
}
```

For more information about all the configuration options, see
the [Kotlin SDK documentation](https://configcat.com/docs/sdk-reference/kotlin/#setting-up-the-configcat-client).

## Need help?

https://configcat.com/support

## Contributing

Contributions are welcome. For more info please read the [Contribution Guideline](CONTRIBUTING.md).

## About ConfigCat

ConfigCat is a feature flag and configuration management service that lets you separate releases from deployments. You
can turn your features ON/OFF using <a href="https://app.configcat.com" target="_blank">ConfigCat Dashboard</a> even
after they are deployed. ConfigCat lets you target specific groups of users based on region, email or any other custom
user attribute.

ConfigCat is a <a href="https://configcat.com" target="_blank">hosted feature flag service</a>. Manage feature toggles
across frontend, backend, mobile, desktop apps. <a href="https://configcat.com" target="_blank">Alternative to
LaunchDarkly</a>. Management app + feature flag SDKs.

- [Official ConfigCat SDKs for other platforms](https://github.com/configcat)
- [Documentation](https://configcat.com/docs)
- [Blog](https://configcat.com/blog)