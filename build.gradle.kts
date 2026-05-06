plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.modmc"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    // Spigot API — compile against lowest supported version
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

    // PacketEvents 2.x — cross-version packet abstraction
    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")

    // SQLite JDBC (bundled with server, but declare for compilation)
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("ModMC-AntiCheat-${project.version}.jar")

        relocate("org.xerial", "com.modmc.anticheat.lib.xerial")

        minimize()
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
