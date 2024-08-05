plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

apply {
    plugin("kotlin-kapt")
    plugin("kotlin-android")
    plugin("com.android.application")
    plugin("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.watueat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.watueat"
        minSdk = 32
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Layout
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")

    // Swipe Refresh
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Retrofit for REST APIs
    implementation ("com.google.code.gson:gson:2.8.5")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // Testing
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test:runner:1.5.2")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

    // Glide for images
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // GoogleMaps
    implementation ("com.google.android.gms:play-services-maps:18.2.0")

    // Firebase
    implementation ("com.google.firebase:firebase-bom:32.7.4")
    implementation ("com.google.firebase:firebase-analytics-ktx")
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.firebase:firebase-storage-ktx")
    implementation ("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation ("com.firebaseui:firebase-ui-storage:8.0.2")
}
