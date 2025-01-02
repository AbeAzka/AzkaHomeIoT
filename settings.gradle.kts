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
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven {url = uri("https://repo.eclipse.org/content/repositories/paho-releases/")}
    }
}

rootProject.name = "Azka Home Iot"
include(":app")
 