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
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:logging"))
    // Note: :core:crypto dependency added in v0.5.0 when SRTP is implemented.
    implementation(libs.kotlinx.coroutines.android)
}
