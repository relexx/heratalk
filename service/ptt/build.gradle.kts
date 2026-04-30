plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "de.relexx.heratalk.service.ptt"
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
    implementation(libs.kotlinx.coroutines.android)
}
