import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.atomicfu)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
}

val isSnapshot: Boolean get() = System.getProperty("snapshot") != null
version = "$version${if (isSnapshot) "-SNAPSHOT" else ""}"

dependencies {
    api(libs.configcat)
    api(libs.openfeature)
    implementation(libs.atomicfu)
    implementation(libs.coroutines)
    implementation(libs.serialization.json)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.ktor.mock)
}

android {
    namespace = "com.configcat"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        version = project.version as String
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    if (providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent &&
        providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword").isPresent
    ) {
        signAllPublications()
    }

    configure(AndroidSingleVariantLibrary("release", sourcesJar = true, publishJavadocJar = true))

    coordinates(project.group as String?, project.name, project.version as String?)

    pom {
        name.set("ConfigCat OpenFeature Provider for Kotlin")
        description.set(
            "OpenFeature Provider that allows ConfigCat to be used with the OpenFeature Kotlin SDK.",
        )
        url.set("https://github.com/configcat/openfeature-kotlin")
        issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/configcat/openfeature-kotlin/issues")
        }
        licenses {
            license {
                name.set("MIT License")
                url.set("https://raw.githubusercontent.com/configcat/openfeature-kotlin/main/LICENSE")
            }
        }
        developers {
            developer {
                id.set("configcat")
                name.set("ConfigCat")
                email.set("developer@configcat.com")
            }
        }
        scm {
            url.set("https://github.com/configcat/openfeature-kotlin")
        }
    }
}
