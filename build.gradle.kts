import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.10"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

application {
    mainClass.set("no.nav.dagpenger.manuell.behandling.AppKt")
}

dependencies {
    implementation(libs.rapids.and.rivers)
    implementation(libs.dp.aktivitetslogg)
    implementation(libs.kotlin.logging)
    implementation("io.getunleash:unleash-client-java:9.2.4")
    implementation(libs.konfig)
    implementation("io.kotest:kotest-property:${libs.versions.kotest.get()}")
    implementation(libs.kotest.assertions.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn("ktlintFormat")
}
tasks.withType<ShadowJar> {
    mergeServiceFiles()
}
