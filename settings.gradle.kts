pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "HeraTalk"

include(":app")
// Core modules
// include(":core:crypto")
// include(":core:model")
// include(":core:network")
// Service modules
// include(":service:audio")
// include(":service:media")
// include(":service:signaling")
// Feature modules
// include(":feature:channel")
// include(":feature:pairing")
// include(":feature:settings")
