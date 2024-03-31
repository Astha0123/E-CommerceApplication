pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven{url= uri("https://jitpack.io")}


    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url= uri("https://jitpack.io"))
        maven (
            url  ="https://phonepe.mycloudrepo.io/public/repositories/phonepe-intentsdk-android"
        )
    }
}


rootProject.name = "E-Commerce Application"
include(":app")
