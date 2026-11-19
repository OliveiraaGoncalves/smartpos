pluginManagement {
    includeBuild("build-logic")
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
    }
}

rootProject.name = "SmartPOS"

include(":app")

include(":core:model")
include(":core:common")
include(":core:domain")
include(":core:security")
include(":core:network")
include(":core:network-mock")
include(":core:local-storage")
include(":core:data")
include(":core:printer")
include(":core:ui")
include(":core:testing")

include(":feature:home")
include(":feature:sale")
include(":feature:transaction-detail")
include(":feature:transactions")
include(":feature:pendings")
include(":feature:more")
include(":feature:receipt")
include(":feature:blocking")
