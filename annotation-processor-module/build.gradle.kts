import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kspVersion: String by project

plugins {
    kotlin("jvm")
}

group = "com.rrain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    testImplementation(kotlin("test"))
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.test {
    useJUnitPlatform()
}
