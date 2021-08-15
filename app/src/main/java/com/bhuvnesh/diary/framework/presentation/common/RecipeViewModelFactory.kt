package com.bhuvnesh.diary.framework.presentation.common

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.interactors.recipeInsertNew.RecipeInsertNewInteractors
import com.bhuvnesh.diary.business.interactors.recipedetail.RecipeDetailInteractors
import com.bhuvnesh.diary.business.interactors.recipelist.RecipeListInteractors
import com.bhuvnesh.diary.framework.presentation.recipeDetail.RecipeDetailViewModel
import com.bhuvnesh.diary.framework.presentation.recipeList.RecipeListViewModel
import com.bhuvnesh.diary.framework.presentation.recipeNew.RecipeInsertViewModel
import com.bhuvnesh.diary.framework.presentation.splash.RecipeNetworkSyncManager
import com.bhuvnesh.diary.framework.presentation.splash.SplashViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
class RecipeViewModelFactory
@Inject
constructor(
    private val recipeListInteractors: RecipeListInteractors,
    private val recipeInsertNewInteractors: RecipeInsertNewInteractors,
    private val recipeDetailInteractors: RecipeDetailInteractors,
    private val recipeNetworkSyncManager: RecipeNetworkSyncManager,
    private val recipeFactory: RecipeFactory,
    private val editor: SharedPreferences.Editor,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {

            RecipeInsertViewModel::class.java -> {
                RecipeInsertViewModel(recipeInsertNewInteractors) as T
            }

            RecipeListViewModel::class.java -> {
                RecipeListViewModel(
                    recipeListInteractors = recipeListInteractors,
                    recipeFactory = recipeFactory,
                    editor = editor,
                    sharedPreferences = sharedPreferences
                ) as T
            }

            RecipeDetailViewModel::class.java -> {
                RecipeDetailViewModel(
                    recipeDetailInteractors = recipeDetailInteractors
                ) as T
            }

            SplashViewModel::class.java -> {
                SplashViewModel(recipeNetworkSyncManager = recipeNetworkSyncManager) as T
            }

            else -> {
                throw IllegalArgumentException("unknown model class $modelClass")
            }
        }
    }
}




















