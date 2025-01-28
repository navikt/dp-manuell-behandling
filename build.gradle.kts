import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow.jar)
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
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
    implementation(libs.konfig)

    implementation("io.getunleash:unleash-client-java:9.3.2")
    implementation("io.prometheus:prometheus-metrics-core:1.3.5")

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.kotest.assertions.core)
    testImplementation("com.approvaltests:approvaltests:24.14.2")
    testImplementation("io.kotest:kotest-property:${libs.versions.kotest.get()}")
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
