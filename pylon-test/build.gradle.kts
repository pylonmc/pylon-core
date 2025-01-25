plugins {
    java
    id("com.gradleup.shadow")
    id("net.minecrell.plugin-yml.bukkit")
    id("xyz.jpenilla.run-paper") version "2.3.0"
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
    depend = listOf("pylon-core")
}

tasks.runServer {
    doFirst {
        val runFolder = project.projectDir.resolve("run")
        runFolder.mkdirs()
        runFolder.resolve("eula.txt").writeText("eula=true")
        val pluginFolder = runFolder.resolve("plugins")
        pluginFolder.mkdirs()
        val archive = project(":pylon-core").tasks.shadowJar.map { it.archiveFile }.get().get().asFile
        archive.copyTo(pluginFolder.resolve(archive.name), overwrite = true)
    }
    maxHeapSize = "4G"
    minecraftVersion("1.21.4")
}
