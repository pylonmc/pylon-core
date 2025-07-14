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
    id("org.jetbrains.dokka") version "2.0.0"
    id("org.jetbrains.dokka-javadoc") version "2.0.0"
}

repositories {
    mavenCentral()
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

    implementation("info.debatty:java-string-similarity:2.0.0")

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

    dokkaJavadocPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:2.0.0")
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

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/docs/kdoc"))
    }
    dokkaPublications.javadoc {
        outputDirectory.set(layout.buildDirectory.dir("dokka/docs/javadoc"))
    }
    dokkaSourceSets.configureEach {
        externalDocumentationLinks.register("Paper") {
            url("https://jd.papermc.io/paper/1.21.4/")
            packageListUrl("https://jd.papermc.io/paper/1.21.4/element-list")
        }
        externalDocumentationLinks.register("Adventure") {
            url("https://javadoc.io/doc/net.kyori/adventure-api/latest/")
            packageListUrl("https://javadoc.io/doc/net.kyori/adventure-api/latest/element-list")
        }
        externalDocumentationLinks.register("InvUI") {
            url("https://invui.javadoc.xenondevs.xyz/")
            packageListUrl("https://invui.javadoc.xenondevs.xyz/element-list")
        }
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/pylonmc/pylon-core")
            remoteLineSuffix.set("#L")
        }
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
    authors = listOf("Pylon team")
    apiVersion = "1.21"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}

// Disable signing for maven local publish
if (project.gradle.startParameter.taskNames.any { it.contains("publishToMavenLocal") }) {
    tasks.withType<Sign>().configureEach {
        enabled = false
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
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