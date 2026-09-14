plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
    implementation(libs.minestom)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass.set("com.bluedragonmc.interactions.coverage.MainKt")
}

val repositoryRoot = rootProject.projectDir.absolutePath

tasks.named<JavaExec>("run") {
    args("--root=$repositoryRoot")
}

tasks.register<JavaExec>("verifyCoverage") {
    group = "verification"
    description = "Fails if COVERAGE.md or the README coverage badge is out of date."
    mainClass.set("com.bluedragonmc.interactions.coverage.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    args("--root=$repositoryRoot", "--check")
}
