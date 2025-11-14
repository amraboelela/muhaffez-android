plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "app.amr.muhaffez"
  compileSdk = 36

  defaultConfig {
    applicationId = "app.amr.muhaffez"
    minSdk = 24
    targetSdk = 36
    versionCode = 2
    versionName = "1.0.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // Enable 16KB page size support for Android 15+ devices
    // Only include arm64-v8a to avoid TensorFlow Lite alignment issues
    ndk {
      //noinspection ChromeOsAbiSupport
      abiFilters += listOf("arm64-v8a")
    }
  }

  signingConfigs {
    create("release") {
      storeFile = file("muhaffez-release-key.jks")
      storePassword = "muhaffez123"
      keyAlias = "muhaffez"
      keyPassword = "muhaffez123"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
    mlModelBinding = true
  }
  androidResources {
    noCompress += listOf("tflite")
  }

  packaging {
    jniLibs {
      // Enable 16 KB page alignment for native libraries
      useLegacyPackaging = false
    }
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)

  // Compose Material Icons Extended
  implementation(libs.androidx.material.icons.extended)
  implementation(libs.androidx.runtime.livedata)

  // TensorFlow Lite for ML model inference (updated for 16KB page size support)
  implementation("org.tensorflow:tensorflow-lite:2.17.0")
  implementation("org.tensorflow:tensorflow-lite-support:0.4.4") {
    exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
    exclude(group = "org.tensorflow", module = "tensorflow-lite")
  }
  implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4") {
    exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
    exclude(group = "org.tensorflow", module = "tensorflow-lite")
  }
  implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")

  // Unit testing with JUnit 5
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

  // If you still want to keep JUnit 4 tests
  testImplementation("junit:junit:4.13.2")

  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.withType<Test> {
  useJUnitPlatform()
}
