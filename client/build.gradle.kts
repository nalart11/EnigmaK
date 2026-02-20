plugins {
    kotlin("jvm") version "2.2.21"
    application
}

group = "org.makeacake"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20240303")
}

kotlin {
    jvmToolchain(24)
}

application {
    mainClass.set("org.makeacake.Main")
}

tasks.test {
    useJUnitPlatform()
}
