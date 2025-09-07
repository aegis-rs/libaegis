plugins {
    kotlin("jvm") version "2.2.0"
    id("maven-publish")
}

group = "dev.teamnight.aegis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.81")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(22)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/aegis-rs/libaegis")
            credentials {
                username = findProperty("github.user") as String?
                    ?: System.getenv("GITHUB_USER")
                password = findProperty("github.token") as String?
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}