import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
//    alias(libs.plugins.library)
    id("kotlin-kapt")


}
// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("C:\\Users\\TANGWAN ELICE\\.android\\debug.keystore")
        }
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String

        }
    }
    namespace = "com.example.gceolmcqs"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gceolmcqs"
        minSdk = 22
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")

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
    val room_version = "2.6.0"
    val fuel_version = "2.3.1"

    implementation ("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation ("androidx.room:room-ktx:$room_version")

    //core
    implementation ("com.github.kittinunf.fuel:fuel:$fuel_version")
    implementation("androidx.cardview:cardview:1.0.0")

//    implementation ("com.github.CamPay.android-sdk:android-sdk:latest.release")
    // Peer dependency
    implementation (libs.rxjava)

    //packages
//    implementation ("com.github.kittinunf.fuel:<package>:<$fuel_version")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.coroutine)
    implementation(libs.liveData)
    implementation(libs.gson)
    implementation(libs.viewModel)
    implementation(libs.okhttp3)
//    implementation(libs.roomKtx)
//    implementation(libs.roomRuntime)
//    annotationProcessor(libs.kapt)
    implementation(libs.volley)
    implementation(libs.parse)
//    implementation(libs.fuel)
//    implementation(libs.fuelPackage)




}