pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
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
 