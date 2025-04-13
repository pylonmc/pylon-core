import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    java
    id("com.gradleup.shadow")
    id("net.minecrell.plugin-yml.bukkit")
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("io.freefair.lombok") version "8.13.1"
}

version = "TEST"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly(project(":pylon-core", "shadow"))
    implementation("org.assertj:assertj-core:3.27.2")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

bukkit {
    name = "PylonTest"
    main = "io.github.pylonmc.pylon.test.PylonTest"
    version = project.version.toString()
    apiVersion = "1.21"
    depend = listOf("PylonCore")
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}

tasks.runServer {
    val runFolder = project.projectDir.resolve("run")
    val testsFailedFile = runFolder.resolve("tests-failed")
    doFirst {
        runFolder.mkdirs()
        runFolder.resolve("eula.txt").writeText("eula=true")
        testsFailedFile.delete()

        val pluginFolder = runFolder.resolve("plugins")
        pluginFolder.mkdirs()
        val archive = project(":pylon-core").tasks.shadowJar.map { it.archiveFile }.get().get().asFile
        archive.copyTo(pluginFolder.resolve(archive.name), overwrite = true)
    }
    maxHeapSize = "4G"
    minecraftVersion("1.21.4")
    doLast {
        runFolder.resolve("gametests").deleteRecursively()
        if (testsFailedFile.exists()) {
            throw GradleException("Tests failed")
        }
    }
}
