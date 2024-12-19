plugins {
    kotlin("jvm") version "1.8.20"
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

kotlin {
    jvmToolchain(17)
}