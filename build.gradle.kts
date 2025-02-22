

// Top-level build.gradle file

buildscript {
    repositories {
        //google()
        mavenCentral() // Jangan dikomentari, masih diperlukan
        //jcenter() // Deprecated, tidak perlu digunakan
        //maven { url = uri("https://jitpack.io") } // Hanya jika menggunakan library dari JitPack
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0") // Android Gradle Plugin (AGP)
        classpath("com.google.gms:google-services:4.3.15") // Jika menggunakan Firebase
        // Other classpath dependencies
    }
}


allprojects {
    repositories {
        google()
        mavenCentral()
        //maven { url = uri("https://jitpack.io") }
    }
}





// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
}

