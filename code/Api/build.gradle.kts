import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

val JDBI_VERSION = "3.37.1"
val PSQL_VERSION = "42.5.4"
val DATETIME_VERSION = "0.4.1"
val PWS_ENCODER_VERSION = "6.0.2"
val JUNIT_VERSION = "4.13.1"

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.3"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
    id("org.sonarqube") version "5.1.0.4882"
}

group = "pt.isel.ps"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

sonar {
    properties {
        val SONAR_TOKEN = "SONAR_TOKEN"
        val SONAR_PROJECT_SERVER_KEY = "SONAR_PROJECT_SERVER_KEY"
        property("sonar.token", System.getenv(SONAR_TOKEN) ?: throw Exception("Missing environment variable '$SONAR_TOKEN'"))
        property("sonar.projectKey", System.getenv(SONAR_PROJECT_SERVER_KEY) ?: throw Exception("Missing environment variable '$SONAR_PROJECT_SERVER_KEY'"))
        property("sonar.organization", "FCUL")
        property("sonar.host.url", "http://localhost:9000")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JDBI
    implementation("org.jdbi:jdbi3-core:$JDBI_VERSION")
    implementation("org.jdbi:jdbi3-kotlin:$JDBI_VERSION")
    implementation("org.jdbi:jdbi3-postgres:$JDBI_VERSION")
    implementation("org.postgresql:postgresql:$PSQL_VERSION")

    // To use Kotlin specific date and time functions
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$DATETIME_VERSION")

    // To get password encode
    implementation("org.springframework.security:spring-security-core:$PWS_ENCODER_VERSION")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:$JUNIT_VERSION")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<JavaExec>("launch") {
    description = "Launches the application"
    group = "launch"
    this.mainClass.set("pt.isel.ps.anonichat.AnonichatApplicationKt")
    classpath = sourceSets["main"].runtimeClasspath
}

task<Exec>("composeUp") {
    commandLine("docker-compose", "up", "-d", "--build", "--force-recreate", "db-compose")
    dependsOn("extractUberJar")
}

task<Exec>("dbTestsWait") {
    commandLine("docker", "exec", "db-compose", "/app/bin/wait-for-postgres.sh", "localhost")
    dependsOn("composeUp")
}

task<Exec>("dbTestsDown") {
    commandLine("docker-compose", "down")
}

task<Copy>("extractUberJar") {
    dependsOn("assemble")
    // opens the JAR containing everything...
    from(zipTree("$buildDir/libs/${rootProject.name}-$version.jar"))
    // ... into the 'build/dependency' folder
    into("build/dependency")
}

tasks.withType<BootJar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named("check") {
    dependsOn("composeUp")
    finalizedBy("dbTestsDown")
}

project.tasks["sonar"].dependsOn "launch"