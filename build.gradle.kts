import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}

val isSnapshot: Boolean get() = System.getProperty("snapshot") != null
version = "$version${if (isSnapshot) "-SNAPSHOT" else ""}"

kotlin {
    androidTarget {
        publishLibraryVariants("release")

        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    js {
        nodejs()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    linuxX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.configcat)
            implementation(libs.openfeature)
            implementation(libs.coroutines)
            implementation(libs.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.coroutines.test)
            implementation(libs.ktor.mock)
        }

        all {
            languageSettings.optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
        }
    }
}

android {
    namespace = "com.configcat"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        version = project.version as String
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dokka {
    dokkaPublications.html {
        suppressInheritedMembers.set(true)
        failOnWarning.set(true)
    }
    dokkaSourceSets.commonMain {
        sourceLink {
            localDirectory.set(file(file("src/commonMain/kotlin")))
            remoteUrl("https://github.com/configcat/openfeature-kotlin/blob/main/src/commonMain/kotlin")
            remoteLineSuffix.set("#L")
        }
    }
}

mavenPublishing {
    if (providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent &&
        providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword").isPresent
    ) {
        signAllPublications()
    }

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = true,
            androidVariantsToPublish = listOf("release"),
        ),
    )

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
