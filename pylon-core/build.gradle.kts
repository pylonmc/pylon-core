plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow")
    id("net.minecrell.plugin-yml.bukkit")
    idea
    `maven-publish`
    signing
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

repositories {
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    api(kotlin("stdlib"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    api(kotlin("reflect"))

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    api("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.20.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.20.0")

    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

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

    doRelocate("co.aikar")

    archiveBaseName = project.name
    archiveClassifier = null
}

bukkit {
    name = "pylon-core"
    main = "io.github.pylonmc.pylon.core.PylonCore"
    version = project.version.toString()
    authors = listOf() // TODO
    apiVersion = "1.21"
}

signing {
    useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
}

centralPortal {
    username = System.getenv("SONATYPE_USERNAME")
    password = System.getenv("SONATYPE_PASSWORD")
    pom {
        description = "The core library for PylonMC plugins."
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