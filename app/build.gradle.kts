plugins {
    id("com.android.application")
    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.1" apply false
}

android {
    namespace = "com.example.musicdiary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.musicdiary"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders.apply {
            set("redirectSchemeName", "spotify-sdk")
            set("redirectHostName", "auth")
        }

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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))
    implementation("com.google.code.gson:gson:2.10")
    implementation ("com.spotify.android:auth:2.1.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation ("com.squareup.picasso:picasso:2.8")
}