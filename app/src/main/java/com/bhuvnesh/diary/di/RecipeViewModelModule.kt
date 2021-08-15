package com.bhuvnesh.diary.di

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.interactors.recipeInsertNew.RecipeInsertNewInteractors
import com.bhuvnesh.diary.business.interactors.recipedetail.RecipeDetailInteractors
import com.bhuvnesh.diary.business.interactors.recipelist.RecipeListInteractors
import com.bhuvnesh.diary.framework.presentation.common.RecipeViewModelFactory
import com.bhuvnesh.diary.framework.presentation.splash.RecipeNetworkSyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
@InstallIn(SingletonComponent::class)
object RecipeViewModelModule {

    @Singleton
    @JvmStatic
    @Provides
    fun provideRecipeViewModelFactory(
        recipeListInteractors: RecipeListInteractors,
        recipeInsertNewInteractors: RecipeInsertNewInteractors,
        recipeDetailInteractors: RecipeDetailInteractors,
        recipeNetworkSyncManager: RecipeNetworkSyncManager,
        recipeFactory: RecipeFactory,
        editor: SharedPreferences.Editor,
        sharedPreferences: SharedPreferences
    ): ViewModelProvider.Factory {
        return RecipeViewModelFactory(
            recipeListInteractors = recipeListInteractors,
            recipeInsertNewInteractors = recipeInsertNewInteractors,
            recipeDetailInteractors = recipeDetailInteractors,
            recipeNetworkSyncManager = recipeNetworkSyncManager,
            recipeFactory = recipeFactory,
            editor = editor,
            sharedPreferences = sharedPreferences
        )
    }

}