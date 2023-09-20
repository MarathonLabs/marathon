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
    }
}

rootProject.name = "SeparateTestModuleApp"
include(":app_impl")
include(":app_api")
include(":app_horizont_impl")
include(":app_horizont_api")
include(":ui_tests")
