plugins { id("com.android.application") }
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
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
}
