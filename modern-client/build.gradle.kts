plugins {
    kotlin("jvm") version "2.2.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20240303")
}

kotlin {
    jvmToolchain(24)
}

tasks.test {
    useJUnitPlatform()
}
