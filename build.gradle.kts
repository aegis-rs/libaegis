import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("com.android.library") version "8.7.3"
    id("maven-publish")
}

group = "dev.teamnight.aegis"
version = "1.0-ALPHA-3"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvmToolchain(22)

    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "libaegis"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.jdk8)
                implementation(libs.bouncycastle)
            }
        }

        val androidMain by getting {
            dependsOn(jvmMain)
        }

        val iosMain by creating {
            dependsOn(commonMain)
        }

        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

android {
    namespace = "dev.teamnight.aegis"
    compileSdk = 34
    defaultConfig {
        minSdk = 33
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
}

publishing {
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