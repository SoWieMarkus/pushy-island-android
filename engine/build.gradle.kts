plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.junit)
}

tasks.test {
    // Tests read the campaign levels and the shared format fixtures from the repository.
    systemProperty("repoRoot", rootDir.absolutePath)
    // Set UPDATE_GENERATED=1 to regenerate docs/level-format/tiles.json and the share code fixture.
    environment("UPDATE_GENERATED", System.getenv("UPDATE_GENERATED") ?: "")
}
