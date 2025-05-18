plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.indodevstudio.azka_home_iot"
    compileSdk = 34


    defaultConfig {

        applicationId = "com.indodevstudio.azka_home_iot"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "2.7"//Last 2.4

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding= true
    }


}



dependencies {
// Untuk Executor bawaan Android (pengganti ListenableFuture)
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("com.getkeepsafe.taptargetview:taptargetview:1.13.3")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.airbnb.android:lottie:5.2.0")
    implementation ("com.github.prolificinteractive:material-calendarview:2.0.1")
    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.4")
    implementation ("androidx.concurrent:concurrent-futures:1.1.0")
    implementation ("com.google.guava:guava:31.0.1-android")
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("com.github.bumptech.glide:glide:4.4.0")
    implementation ("androidx.core:core-splashscreen:1.0.0")
    implementation ("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0")
    implementation ("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation ("com.google.android.gms:play-services-auth:18.1.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation ("com.github.MikeOrtiz:TouchImageView:3.0.2")
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("androidx.test:core-ktx:1.6.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("com.github.hannesa2:paho.mqtt.android:3.3.5")
    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.retrofit2:converter-gson:2.5.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.privacysandbox.tools:tools-core:1.0.0-alpha12")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}



