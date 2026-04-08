import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

val hasGoogleServicesConfig = listOf(
    file("google-services.json"),
    file("src/google-services.json"),
    file("src/debug/google-services.json"),
    file("src/release/google-services.json"),
).any { it.exists() }

if (hasGoogleServicesConfig) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.lifecycle("google-services.json not found in composeApp; skipping Google Services plugin for this build.")
}

kotlin {
    @Suppress("DEPRECATION")
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.firebase.messaging)
            implementation("com.google.android.gms:play-services-maps:19.0.0")
            implementation("com.google.maps.android:maps-compose:6.4.1")
            implementation(libs.credentials)
            implementation(libs.credentials.play.services)
            implementation(libs.googleid)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            @Suppress("DEPRECATION")
            implementation(compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            @Suppress("DEPRECATION")
            implementation(compose.materialIconsExtended)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.supabase.auth.kt)
            implementation(libs.supabase.compose.auth)
            implementation(libs.supabase.storage.kt)
            implementation(libs.supabase.realtime.kt)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.company.carryon"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.company.carryon"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        val localProperties = Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) load(file.inputStream())
        }
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = localProperties.getProperty("GOOGLE_MAPS_API_KEY", "")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    lint {
        // Lint's Kotlin analyzer bundled with AGP can lag behind project Kotlin metadata.
        // Keep lint reports, but don't fail the build on analyzer incompatibilities.
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}
// Temporary workaround: AGP lint Kotlin analyzer expects Kotlin metadata 2.1.0,
// while dependencies are compiled with 2.3.0. Disable lint tasks so build/test
// remains unblocked until AGP/Kotlin are aligned.
tasks.matching { it.name.startsWith("lint") }.configureEach {
    enabled = false
}
