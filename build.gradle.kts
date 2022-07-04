@file:Suppress("SpellCheckingInspection", "GradleDependency", "AndroidGradlePluginVersion")

/*
 * This file configures the build system that creates your Android app.
 * The syntax is Kotlin, but many of the idioms are likely unfamiliar.
 * You do not need to understand the contents of this file, nor should you modify it.
 * Any changes will be overwritten during official grading.
 */

buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("com.fasterxml.jackson.core:jackson-databind:2.13.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
    }
}
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}
allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
tasks.register<Delete>("clean") {
    delete = setOf(rootProject.buildDir)
}
