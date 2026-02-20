plugins {
    kotlin("jvm") version "2.2.21"
}

group = "org.makeacake"
version = "1.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.makeacake.Main")
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-core:2.24.1")
    implementation("org.apache.logging.log4j:log4j-api:2.24.1")
    implementation("org.json:json:20240303")
}

kotlin {
    jvmToolchain(24)
}

tasks.test {
    useJUnitPlatform()
}