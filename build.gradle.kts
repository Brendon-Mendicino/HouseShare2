// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.daggerHilt) apply false
    alias(libs.plugins.android.junit5) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.idea)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

