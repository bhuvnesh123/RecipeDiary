package com.bhuvnesh.diary.di

import androidx.room.Room
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.framework.dataSource.cache.database.RecipeDatabase
import com.bhuvnesh.diary.framework.dataSource.data.RecipesDataFactory
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ProductionModule::class]
)
object TestModule {

//    @JvmStatic
//    @Singleton
//    @Provides
//    fun provideHiltTestApplication():HiltTestApplication{
//        return HiltTestApplication()
//    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeDb(app: HiltTestApplication): RecipeDatabase {
        return Room
            .inMemoryDatabaseBuilder(app, RecipeDatabase::class.java)
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
    fun provideRecipeDataFactory(
        application: HiltTestApplication,
        recipeFactory: RecipeFactory
    ): RecipesDataFactory {
        return RecipesDataFactory(application, recipeFactory)
    }

}

