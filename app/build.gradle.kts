plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.2" apply false
}

android {
    namespace = "com.example.plantique"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.plantique"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")

    // Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))

    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database")

    // Firebase Storage
    implementation("com.google.firebase:firebase-storage")

    // Firebase Firestore - Add this dependency to fix the error
    implementation("com.google.firebase:firebase-firestore")

    // Image loading libraries
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.picasso:picasso:2.8")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Apply Google Services plugin
apply(plugin = "com.google.gms.google-services")