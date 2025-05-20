plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

group = "io.github.pylonmc"

repositories {
    mavenCentral()
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    compileOnly(project(":pylon-core"))
}

kotlin {
    jvmToolchain(21)
}