import org.jetbrains.kotlin.config.JvmTarget

plugins {
    kotlin("multiplatform") version "1.6.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
//    kotlin("plugin.serialization") version "1.6.20"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

kotlin {
    jvm {
        withJava()
        configure(listOf(compilations["main"], compilations["test"])) {
            kotlinOptions {
                jvmTarget = "11"

            }
            compileKotlinTask.targetCompatibility = "11"
        }
    }

    js(IR) {
        configure(listOf(compilations["main"], compilations["test"])) {
        }
        binaries.executable()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    val ktorVersion = "2.0.0"
    val jUnitJupiterVersion = "5.8.2"
    val kotlinCoroutinesVersion = "1.6.1"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("reflect"))

                // Ktor
                implementation("io.ktor:ktor-io:$ktorVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jsMain by getting

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val jvmMain by getting {

            dependencies {

                implementation(kotlin("stdlib"))

                // Server
                // https://mvnrepository.com/artifact/io.ktor/ktor-server-core
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
//                implementation("io.ktor:ktor-server-servlet:$ktorVersion")

                implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
                implementation("io.ktor:ktor-server-call-id:$ktorVersion")
                implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-server-auto-head-response:$ktorVersion")
                implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
                implementation("io.ktor:ktor-server-compression:$ktorVersion")
                implementation("io.ktor:ktor-server-locations:$ktorVersion")
                implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
                implementation("io.ktor:ktor-server-websockets:$ktorVersion")
            }

        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter-params:$jUnitJupiterVersion")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitJupiterVersion")

                implementation("io.ktor:ktor-server-test-host:$ktorVersion") {
                    exclude("org.jetbrains.kotlin", "kotlin-test-junit")
                }

                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            }
        }


        all {
            languageSettings {
                progressiveMode = true
                optIn("io.ktor.server.locations.KtorExperimentalLocationsAPI")
            }
        }
    }
}



tasks.getByName<Test>("jvmTest") {
    useJUnitPlatform()
}
