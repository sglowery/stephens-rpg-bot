plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "tech.stephenlowery"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":telegram"))
}
