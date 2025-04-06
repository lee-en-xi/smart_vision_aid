// Top-level build file
plugins {
    id("com.android.application") version "8.2.2" apply false  // Use stable version
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")  // Match plugin version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}