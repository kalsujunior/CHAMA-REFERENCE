plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
val reliveApiBaseUrl = providers.gradleProperty("reliveApiBaseUrl").orNull ?: "https://relive-chama-api.kalsujunior.workers.dev"

android {
    namespace = "com.kalsu.relive"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.kalsu.relive"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "0.4.0"
        buildConfigField("String", "RELIVE_API_BASE_URL", "\"$reliveApiBaseUrl\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("io.github.sceneview:sceneview:4.74.0")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
