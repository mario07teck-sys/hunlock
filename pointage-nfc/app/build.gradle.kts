plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "fr.pointage.nfc"
    compileSdk = 36
    defaultConfig {
        // Never change applicationId once on the Play Store: it is the app's identity there.
        applicationId = "fr.pointage.nfc"
        minSdk = 26
        targetSdk = 36
        // Play refuses an upload whose versionCode is not higher than the last one: CI run number always grows.
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 1
        versionName = "1.0"
    }
    // Upload key, given by CI secrets (see README). Without it, release builds stay unsigned.
    val keystore = System.getenv("KEYSTORE_FILE")
    signingConfigs {
        if (keystore != null) create("upload") {
            storeFile = file(keystore)
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "upload"
            keyPassword = System.getenv("KEYSTORE_PASSWORD")
        }
    }
    buildTypes {
        release { signingConfig = signingConfigs.findByName("upload") }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
