plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.relexx.heratalk.feature.settings"
    compileSdk =
        libs.versions.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
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
    // appcompat hosts AppCompatDelegate.setApplicationLocales(...) which is the
    // canonical AndroidX entry point for runtime locale switching across all
    // supported API levels (impl-plan-v0.1.0.md §C5).
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.compose.ui.tooling)
}
