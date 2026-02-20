plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "EnigmaK"

include("server")
include("client")
include("modern-client")