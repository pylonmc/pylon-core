plugins {
    kotlin("jvm") version "2.1.10" apply false
    id("com.gradleup.shadow") version "8.3.2" apply false
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" apply false
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