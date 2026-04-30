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
include(":core:model")
include(":core:logging")
include(":core:crypto")
include(":core:identity")
include(":core:ui")

// Service modules
include(":service:lifecycle")
include(":service:discovery")
include(":service:transport")
include(":service:signaling")
include(":service:media")
include(":service:audio")
include(":service:ptt")
include(":service:relay")

// Feature modules
include(":feature:pairing")
include(":feature:channel")
include(":feature:direct")
include(":feature:settings")
