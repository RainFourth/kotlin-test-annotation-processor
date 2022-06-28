
pluginManagement {

    val kotlinVersion: String by settings
    val kspVersion: String by settings

    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

}


rootProject.name = "kotlin-test-annotation-processor"


include(":main-project-module")
include(":annotation-processor-module")
