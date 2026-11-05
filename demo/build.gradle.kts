plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(libs.minestom)
    implementation(libs.adventure.api)
    implementation(project(":lib"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.jvm.get().toInt())
    }
}

application {
    mainClass = "com.bluedragonmc.interactions.DemoServer"
}
