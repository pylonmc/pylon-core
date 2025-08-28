plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

group = "io.github.pylonmc"

repositories {
    mavenCentral()
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    compileOnly(project(":plugin"))
}

kotlin {
    jvmToolchain(21)
}