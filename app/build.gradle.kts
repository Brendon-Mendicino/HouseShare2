import com.google.protobuf.gradle.id
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("idea")
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHilt)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

android {
    namespace = "lol.terabrendon.houseshare2"
    compileSdk = 36

    defaultConfig {
        applicationId = "lol.terabrendon.houseshare2"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:9090/\"")
        }
        release {
            // Just for testing...
            buildConfigField("String", "BASE_URL", "\"https://10.0.2.2:9090/\"")

            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the
            // Android Gradle plugin.
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.26.1"
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project. See
    // https://github.com/google/protobuf-gradle-plugin#customizing-protobuf-compilation
    // for more information.
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                id("java") {
                    option("lite")
                }
                id("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

// Protobuf broken with newer ksp version
// Github issue: https://github.com/google/ksp/issues/1590
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val capName = variant.name.capitalized()
            tasks.getByName<KotlinCompile>("ksp${capName}Kotlin") {
                setSource(tasks.getByName("generate${capName}Proto").outputs)
            }
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
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
    implementation(libs.androidx.compose.animation)
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
    implementation(libs.androidx.compose.ui.text.google.fonts)
    ksp(libs.com.google.dagger.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
//    implementation(libs.androidx.hilt.lifecycle.viewmodel)
//    kapt(libs.androidx.hilt.compiler)

    // Proto DataSource
    implementation(libs.androidx.datastore)
    implementation(libs.com.google.protobuf.javalite)
    implementation(libs.com.google.protobuf.kotlin.lite)

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

    // Desugar
    coreLibraryDesugaring(libs.com.android.tools.desugar.jdk.libs)

    ksp(libs.com.google.dagger.hilt.compiler)

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