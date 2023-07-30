plugins {
    kotlin("jvm") version "1.8.20"
    application
}

group = "tech.stephenlowery"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":telegram"))
}

kotlin {
    jvmToolchain(17)
}