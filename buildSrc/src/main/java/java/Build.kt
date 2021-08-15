package dependencies


object Build {
    val build_tools = "com.android.tools.build:gradle:${Versions.gradle}"
    val kotlin_gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val google_services = "com.google.gms:google-services:${Versions.play_services}"
    val junit5 = "de.mannodermaus.gradle.plugins:android-junit5:1.3.2.0"
    val crashlytics =
        "com.google.firebase:firebase-crashlytics-gradle:${Versions.firebase_crashlytics}"
    val hilt_plugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt_version}"
    val safe_args_gradle_plugin =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.nav_components}"
}