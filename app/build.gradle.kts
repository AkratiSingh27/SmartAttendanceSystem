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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //Add cameraX and ml-kit dependancies
    implementation ("androidx.camera:camera-core:1.1.0-beta02")

    implementation ("androidx.camera:camera-camera2:1.1.0-beta02")

    implementation ("androidx.camera:camera-lifecycle:1.1.0-beta02")

    implementation ("androidx.camera:camera-video:1.1.0-beta02")

    implementation ("androidx.camera:camera-view:1.1.0-beta02")

    implementation ("androidx.camera:camera-extensions:1.1.0-beta02")
    implementation ("com.google.mlkit:barcode-scanning:16.1.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.google.zxing:core:3.4.1")


}