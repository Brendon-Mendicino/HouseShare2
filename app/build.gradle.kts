import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    id("idea")
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.google.services)
    alias(libs.plugins.google.crashlytics)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

val keystoreProperties = Properties()
val keystoreFile = rootProject.file("keystore.properties")

if (keystoreFile.exists()) {
    keystoreProperties.load(FileInputStream(keystoreFile))
}

android {
    namespace = "lol.terabrendon.houseshare2"
    compileSdk = 36

    defaultConfig {
        applicationId = "lol.terabrendon.houseshare2"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("upload") {
            if (keystoreFile.exists()) {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    buildTypes {
        debug {
//            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:9090/\"")
            // Local server
//            buildConfigField("String", "BASE_URL", "\"http://192.168.1.150:9090/\"")
            // Remote server
            buildConfigField("String", "BASE_URL", "\"https://houseshare.hollowinsidepizza.xyz/\"")
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            // Just for testing...
            buildConfigField("String", "BASE_URL", "\"https://houseshare.hollowinsidepizza.xyz/\"")

            ndk.debugSymbolLevel = "FULL"

            isDebuggable = false

            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the
            // Android Gradle plugin.
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // This ensures Crashlytics gets the mapping file automatically
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = true
            }

            signingConfig = signingConfigs.getByName("upload")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    androidResources {
        generateLocaleConfig = true
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_0
    }
    sourceSets.all {
//        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Appcompat
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.appcompat.resources)

//    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.bundles.kotlin.result)

    implementation(libs.com.google.dagger.hilt)
    implementation(libs.androidx.ui.text.google.fonts)
    ksp(libs.com.google.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
//    implementation(libs.androidx.hilt.lifecycle.viewmodel)
//    kapt(libs.androidx.hilt.compiler)

    // DataSource
    implementation(libs.androidx.datastore)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Coil (image loader)
    implementation(libs.coil)

    implementation(libs.bundles.aformvalidator)
    ksp(libs.aformvalidator.processor)

    // Http Request
    implementation(libs.bundles.retrofit)

    // Oauth2
//    implementation(libs.appauth)

    // Navigation3
    implementation(libs.bundles.navigation3)

    // Timber
    implementation(libs.timber)

    // Firebase
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.bundles.firebase)

    // Desugar
    coreLibraryDesugaring(libs.com.android.tools.desugar.jdk.libs)

    ksp(libs.com.google.dagger.hilt.compiler)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.unittest)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}