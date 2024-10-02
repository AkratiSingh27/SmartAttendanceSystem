plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.maekotech.smartattendancesystem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.maekotech.smartattendancesystem"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // CameraX dependencies
    implementation("androidx.camera:camera-core:1.1.0-beta02")
    implementation("androidx.camera:camera-camera2:1.1.0-beta02")
    implementation("androidx.camera:camera-lifecycle:1.1.0-beta02")
    implementation("androidx.camera:camera-video:1.1.0-beta02")
    implementation("androidx.camera:camera-view:1.1.0-beta02")
    implementation("androidx.camera:camera-extensions:1.1.0-beta02")

    // Barcode scanning
    implementation("com.google.mlkit:barcode-scanning:16.1.0")

    // ZXing dependencies
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.4.1")

    // Retrofit dependencies
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp dependency
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Gson dependency
    implementation("com.google.code.gson:gson:2.8.9")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")

    // Scalars converter for handling plain text responses
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")

    implementation ("com.android.volley:volley:1.2.1")

    implementation ("androidx.camera:camera-core:1.0.0")
    implementation ("androidx.camera:camera-camera2:1.0.0")
    implementation ("androidx.camera:camera-lifecycle:1.0.0")
    implementation ("androidx.camera:camera-view:1.0.0")

}
