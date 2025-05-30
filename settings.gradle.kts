import java.net.URI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            name = "Central Portal Snapshots"
            url = URI("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}

rootProject.name = "configcat-openfeature-provider"
