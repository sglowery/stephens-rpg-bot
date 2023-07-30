plugins {
    kotlin("jvm") version "1.8.20"
    groovy
}

group = "tech.stephenlowery"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.1.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    testImplementation("org.spockframework.spock:spock-core:spock-2.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.objenesis:objenesis:3.3")
    testImplementation("io.github.joke:spock-mockable:2.3.2")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs?.add("-javaagent:${classpath.find { it.name.contains("spock-mockable") }?.absolutePath}")
}

kotlin {
    jvmToolchain(17)
}