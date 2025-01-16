plugins {
    idea
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

group = "io.github.pylon-paper"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    api(kotlin("stdlib"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    api(kotlin("reflect"))

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    api("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.20.0")

    testImplementation(kotlin("test"))
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        javaParameters = true
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

tasks.shadowJar {
    mergeServiceFiles()

    fun doRelocate(lib: String) {
        relocate(lib, "io.github.pylonmc.pylon.core.shadowlibs.$lib")
    }

    archiveBaseName = rootProject.name
}

bukkit {
    name = rootProject.name
    main = "io.github.pylonmc.pylon.core.PylonCore"
    version = project.version.toString()
    authors = listOf() // TODO
    apiVersion = "1.21"
}

tasks.runServer {
    maxHeapSize = "4G"
    minecraftVersion("1.21.4")
}