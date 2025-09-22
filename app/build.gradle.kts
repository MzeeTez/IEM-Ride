plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.iemride"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.iemride"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }

    // Correct Kotlin DSL syntax for packagingOptions
    packagingOptions {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes.add("com/j256/ormlite/core/README.txt")
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.2")

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")

    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging:23.4.0")

    // ExoPlayer for video playback
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-common:1.2.1")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // OpenStreetMap (OSMDroid)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.18")
    // Correct Kotlin DSL syntax for excluding a transitive dependency
    implementation("org.osmdroid:osmdroid-geopackage:6.1.18") {
        exclude(group = "com.j256.ormlite", module = "ormlite-core")
    }

    // HTTP client for FCM notifications
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Location services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
