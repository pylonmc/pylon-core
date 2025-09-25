pluginManagement {
    val useLocalDokka = extra.get("usePylonDokka").toString().toBoolean()

    repositories {
        if (useLocalDokka) mavenLocal()
        gradlePluginPortal()
    }

    val dokkaVersion = if (useLocalDokka) "2.1.0-pylon-SNAPSHOT" else "2.0.0"
    plugins {
        id("org.jetbrains.dokka") version dokkaVersion
        id("org.jetbrains.dokka-javadoc") version dokkaVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "pylon-core-root"

include("pylon-core")
include("test")
include("nms")
include("dokka-plugin")