import org.gradle.kotlin.dsl.runServer

plugins {
    id("java")
    id("com.gradleup.shadow")
    id("net.minecrell.plugin-yml.bukkit")
    id("xyz.jpenilla.run-paper")
}

version = "TEST"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly(project(":pylon-core"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

bukkit {
    name = project.name
    main = "io.github.pylonmc.pylon.test.TestAddon"
    version = project.version.toString()
    apiVersion = "1.21"
    depend = listOf("Pylon-Core")
}

tasks.runServer {
    doFirst {
        val runFolder = project.projectDir.resolve("run")
        runFolder.mkdirs()
        runFolder.resolve("eula.txt").writeText("eula=true")
        val pluginFolder = runFolder.resolve("plugins")
        pluginFolder.mkdirs()
        copy {
            from(project(":pylon-core").tasks.shadowJar.map { it.archiveFile })
            into(pluginFolder)
        }
    }
    maxHeapSize = "4G"
    minecraftVersion("1.21.4")
}