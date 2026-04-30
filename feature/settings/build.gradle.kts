plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.relexx.heratalk.feature.settings"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += listOf("-Xexplicit-api=strict")
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:identity"))
    // Note: :feature:pairing is NOT a dependency of :feature:settings to avoid circular deps.
    // DisplayNameScreen from :feature:pairing is reused via shared navigation in :app.

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.androidx.datastore)

    debugImplementation(libs.compose.ui.tooling)
}
