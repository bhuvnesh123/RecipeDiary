package dependencies.dependencies

import dependencies.Versions
import dependencies.Versions.compose
import dependencies.Versions.compose_constraintlayout

object Dependencies {
    val kotlin_standard_library = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    val ktx = "androidx.core:core-ktx:${Versions.ktx}"
    val kotlin_coroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines_version}"
    val kotlin_coroutines_android =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines_version}"
    val kotlin_coroutines_play_services =
        "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.coroutines_play_services}"
    val navigation_fragment =
        "androidx.navigation:navigation-fragment-ktx:${Versions.nav_components}"
    val navigation_runtime = "androidx.navigation:navigation-runtime:${Versions.nav_components}"
    val navigation_ui = "androidx.navigation:navigation-ui-ktx:${Versions.nav_components}"
    val navigation_dynamic =
        "androidx.navigation:navigation-dynamic-features-fragment:${Versions.nav_components}"
    val material_dialogs = "com.afollestad.material-dialogs:core:${Versions.material_dialogs}"
    val material_dialogs_input =
        "com.afollestad.material-dialogs:input:${Versions.material_dialogs}"
    val room_runtime = "androidx.room:room-runtime:${Versions.room}"
    val room_ktx = "androidx.room:room-ktx:${Versions.room}"
    val play_core = "com.google.android.play:core:${Versions.play_core}"
    val leak_canary = "com.squareup.leakcanary:leakcanary-android:${Versions.leak_canary}"
    val firebase_firestore =
        "com.google.firebase:firebase-firestore-ktx:${Versions.firebase_firestore}"
    val firebase_auth = "com.google.firebase:firebase-auth:${Versions.firebase_auth}"
    val firebase_analytics = "com.google.firebase:firebase-analytics:${Versions.firebase_analytics}"
    val lifecycle_runtime = "androidx.lifecycle:lifecycle-runtime:${Versions.lifecycle_version}"
    val lifecycle_coroutines =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle_version}"
    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit2_version}"
    val retrofit_gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit2_version}"
    val markdown_processor = "com.yydcdut:markdown-processor:${Versions.markdown_processor}"
    val hilt = "com.google.dagger:hilt-android:${Versions.hilt_version}"
    val firebase_bom = "com.google.firebase:firebase-bom:${Versions.firebase_bom}"
    val firebase_analytics_ktx = "com.google.firebase:firebase-analytics-ktx"
    val firebase_crashlytics_ktx = "com.google.firebase:firebase-crashlytics-ktx"

    val compose_ui = "androidx.compose.ui:ui:$compose"
    val compose_foundation = "androidx.compose.foundation:foundation:$compose"
    val compose_runtime_livedata = "androidx.compose.runtime:runtime-livedata:$compose"
    val compose_runtime_rxjava2 = "androidx.compose.runtime:runtime-rxjava2:$compose"
    val compose_material = "androidx.compose.material:material:$compose"
    val compose_material_icons_core = "androidx.compose.material:material-icons-core:$compose"
    val compose_material_icons_extended =
        "androidx.compose.material:material-icons-extended:$compose"
    val compose_compiler = "androidx.compose.compiler:compiler:$compose"
    val compose_tooling = "androidx.compose.ui:ui-tooling:$compose"
    val compose_constraintLayout =
        "androidx.constraintlayout:constraintlayout-compose:$compose_constraintlayout"
}





