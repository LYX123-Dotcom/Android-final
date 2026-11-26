plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
}

android {
    namespace = "cn.campus.ledger"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "cn.campus.ledger"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.fragment:fragment:1.3.6")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.room:room-runtime:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
}
