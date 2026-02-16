plugins {
    kotlin("jvm") version "2.0.0"
    groovy
}

group = "tech.stephenlowery"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.spockframework.spock:spock-core:spock-2.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.objenesis:objenesis:3.3")
    testImplementation("io.github.joke:spock-mockable:2.3.2")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs?.add("-javaagent:${classpath.find { it.name.contains("spock-mockable") }?.absolutePath}")
}
