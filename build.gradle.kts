// Top-level build.gradle file

buildscript {

    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Add JitPack repository
    }
    dependencies {

        classpath("com.google.gms:google-services:4.3.5")
        classpath ("com.android.tools.build:gradle:7.0.4")

    }
}

subprojects {
    repositories {
        google()               // Google's Maven repository
        mavenCentral()         // Maven Central repository
        maven { url = uri ("https://jitpack.io") }  // JitPack repository for GitHub-based dependencies

    }
}




// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}

