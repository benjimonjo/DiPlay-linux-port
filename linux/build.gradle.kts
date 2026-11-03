plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(21)
}

compose.desktop {
    application {
        mainClass = "com.shilapi.diplay.linux.MainKt"
        nativeDistributions {
            packageName = "DiPlay"
            packageVersion = "0.1.0"
            description = "Native Linux CarPlay receiver"
            vendor = "DiPlay contributors"
            linux {
                debPackageVersion = "0.1.0"
                rpmPackageVersion = "0.1.0"
            }
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
