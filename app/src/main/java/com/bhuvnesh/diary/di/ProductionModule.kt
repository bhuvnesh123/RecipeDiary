package com.bhuvnesh.diary.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.bhuvnesh.diary.framework.dataSource.cache.database.RecipeDatabase
import com.bhuvnesh.diary.framework.dataSource.preferences.PreferenceKeys
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton


/*
    Dependencies in this class have test fakes for ui tests. See "TestModule.kt" in
    androidTest dir
 */
@ExperimentalCoroutinesApi
@FlowPreview
@InstallIn(SingletonComponent::class)
@Module
object ProductionModule {


    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeDb(@ApplicationContext appContext: Context): RecipeDatabase {
        return Room
            .databaseBuilder(appContext, RecipeDatabase::class.java, RecipeDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPreferences(
        @ApplicationContext appContext: Context
    ): SharedPreferences {
        return appContext
            .getSharedPreferences(
                PreferenceKeys.RECIPE_PREFERENCES,
                Context.MODE_PRIVATE
            )
    }
}

