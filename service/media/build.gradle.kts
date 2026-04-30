plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.relexx.heratalk.service.media"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
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
    implementation(project(":core:logging"))
    // Note: :core:crypto dependency added in v0.5.0 when SRTP is implemented.
    implementation(libs.kotlinx.coroutines.android)
}
