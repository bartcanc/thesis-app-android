

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.thesisapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.thesisapp"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.24")
    implementation ("androidx.core:core-ktx:1.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1") 
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.0")
    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-okhttp:2.3.3")
    implementation("io.ktor:ktor-client-websockets:2.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.google.android.material:material:1.9.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.androidx.junit.ktx)

    testImplementation ("junit:junit:4.13.2")

    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation ("androidx.test:runner:1.5.2")
    androidTestImplementation ("androidx.test:rules:1.5.0")

    // Robolectric - Uruchamianie testów jednostkowych w JVM
    testImplementation("org.robolectric:robolectric:4.10")

    // Mockito Core - W razie potrzeby mockowania obiektów
    testImplementation("org.mockito:mockito-core:5.6.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.runtime.saved.instance.state)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}