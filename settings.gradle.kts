pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
        maven (uri("https://jitpack.io"))
        maven (uri("https://plugins.gradle.org/m2/"))

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        gradlePluginPortal()
        google()
        mavenCentral()
        maven (uri("https://jitpack.io"))
        maven (uri("https://plugins.gradle.org/m2/"))


    }
}

rootProject.name = "GCEOLMCQS"
include(":app")
 