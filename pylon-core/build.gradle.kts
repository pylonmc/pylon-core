import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm")
    `java-library`
    id("com.gradleup.shadow")
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
    idea
    `maven-publish`
    signing
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

repositories {
    maven("https://repo.xenondevs.xyz/releases") {
        name = "InvUI"
    }
    maven("https://repo.aikar.co/content/groups/aikar/") {
        name = "Aikar"
    }
}

dependencies {
    fun paperLibraryApi(dependency: Any) {
        paperLibrary(dependency)
        compileOnlyApi(dependency)
    }

    runtimeOnly(project(":nms"))

    paperLibraryApi("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    paperLibraryApi("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.20.0")
    paperLibrary("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.20.0")
    paperLibrary("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    paperLibraryApi("xyz.xenondevs.invui:invui-core:1.45")
    // see https://github.com/NichtStudioCode/InvUI/blob/main/inventoryaccess/inventory-access/src/main/java/xyz/xenondevs/inventoryaccess/version/InventoryAccessRevision.java
    paperLibrary("xyz.xenondevs.invui:inventory-access-r22:1.45:remapped-mojang")
    paperLibraryApi("xyz.xenondevs.invui:invui-kotlin:1.45")

    testImplementation(kotlin("test"))
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
    testImplementation("net.kyori:adventure-api:4.20.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.20.0")
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

    exclude("kotlin/**")

    archiveBaseName = project.name
    archiveClassifier = null
}

paper {
    generateLibrariesJson = true

    name = "PylonCore"
    loader = "io.github.pylonmc.pylon.core.PylonLoader"
    bootstrapper = "io.github.pylonmc.pylon.core.PylonBootstrapper"
    main = "io.github.pylonmc.pylon.core.PylonCore"
    version = project.version.toString()
    authors = listOf() // TODO
    apiVersion = "1.21"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}

signing {
    useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
}

centralPortal {
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")
    pom {
        description = "The core library for Pylon addons."
        url = "https://github.com/pylonmc/pylon-core"
        licenses {
            license {
                name = "GNU Lesser General Public License Version 3"
                url = "https://www.gnu.org/licenses/lgpl-3.0.txt"
            }
        }
        developers {
            developer {
                id = "PylonMC"
                name = "PylonMC"
                organizationUrl = "https://github.com/pylonmc"
            }
        }
        scm {
            connection = "scm:git:git://github.com/pylonmc/pylon-core.git"
            developerConnection = "scm:git:ssh://github.com:pylonmc/pylon-core.git"
            url = "https://github.com/pylonmc/pylon-core"
        }
    }
}