import io.github.andreabrighi.gradle.gitsemver.conventionalcommit.ConventionalCommit

plugins {
    alias(libs.plugins.git.sensitive.semantic.versioning)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    application
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.github.andreabrighi:conventional-commit-strategy-for-git-sensitive-semantic-versioning-gradle-plugin:1.0.15")
    }
}

gitSemVer {
    maxVersionLength.set(20)
    commitNameBasedUpdateStrategy(ConventionalCommit::semanticVersionUpdate)
}

repositories {
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.konsist)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.dotenv.kotlin)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.content.negotiation.jvm)
    implementation(libs.kmongo.coroutine.serialization)
    implementation(libs.auth0.jwt)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "it.unibo.MainKt"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("config/detekt/detekt.yaml")
}

tasks.register("printVersion") {
    val version = project.version
    doLast {
        println("Project version: $version")
    }
}

tasks.jar {
    archiveFileName.set("app.jar")
    manifest {
        attributes["Main-Class"] = application.mainClass.get() // or specify your main class directly
    }

    // Include all runtime dependencies into the JAR file
    from(
        configurations.runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) },
    )

    // Optionally, include your compiled classes (if not already included by default)
    from(sourceSets.main.get().output)

    // Ensure the JAR is built as a single fat JAR
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
