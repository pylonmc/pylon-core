plugins {
    kotlin("jvm") version "2.1.10" apply false
    id("com.gradleup.shadow") version "8.3.2" apply false
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" apply false
    id("com.gradleup.nmcp.aggregation") version "1.1.0"
    idea
}

allprojects {
    group = "io.github.pylonmc"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "papermc"
        }
    }
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("SONATYPE_USERNAME")
        password = System.getenv("SONATYPE_PASSWORD")
        publishingType = "AUTOMATIC"
    }
    publishAllProjectsProbablyBreakingProjectIsolation()
}