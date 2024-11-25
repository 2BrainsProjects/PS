plugins {
    kotlin("jvm") version "1.9.22"
    id("org.sonarqube") version "5.1.0.4882"
}

val OKHHTP_VERSION = "4.5.0"
val GSON_VERSION = "2.8.8"
val COROUTINES_VERSION = "1.7.3"

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("com.squareup.okhttp3:okhttp:$OKHHTP_VERSION")
    implementation("com.google.code.gson:gson:$GSON_VERSION")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$COROUTINES_VERSION")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
sonar {
    properties {
        val SONAR_TOKEN = "SONAR_TOKEN"
        val SONAR_PROJECT_CLIENT_KEY = "SONAR_PROJECT_CLIENT_KEY"
        property("sonar.token", System.getenv(SONAR_TOKEN) ?: throw Exception("Missing environment variable '$SONAR_TOKEN'"))
        property("sonar.projectKey", System.getenv(SONAR_PROJECT_CLIENT_KEY) ?: throw Exception("Missing environment variable '$SONAR_PROJECT_CLIENT_KEY'"))
        property("sonar.organization", "FCUL")
        property("sonar.host.url", "http://localhost:9000")
    }
}

tasks.register<JavaExec>("launch") {
    description = "Launches the application"
    group = "launch"
    this.mainClass.set("MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

task<Copy>("extractUberJar") {
    dependsOn("assemble")
    // opens the JAR containing everything...
    from(zipTree("$buildDir/libs/${rootProject.name}-$version.jar"))
    // ... into the 'build/dependency' folder
    into("build/dependency")
}

project.tasks["sonar"].dependsOn "launch"
