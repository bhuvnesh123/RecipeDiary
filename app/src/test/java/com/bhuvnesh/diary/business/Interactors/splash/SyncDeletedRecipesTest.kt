package com.bhuvnesh.diary.business.Interactors.splash

import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.interactors.splash.SyncDeletedRecipes
import com.bhuvnesh.diary.di.DependencyContainer
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. deleteNetworkRecipes_confirmCacheSync()
    a) select some recipes for deleting from network
    b) delete from network
    c) perform sync
    d) confirm recipes from cache were deleted
 */

@InternalCoroutinesApi
class SyncDeletedRecipesTest {

    // system in test
    private val syncDeletedRecipes: SyncDeletedRecipes

    // dependencies
    private val dependencyContainer: DependencyContainer
    private val recipeCacheDataSource: RecipeCacheDataSource
    private val recipeNetworkDataSource: RecipeNetworkDataSource
    private val recipeFactory: RecipeFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        recipeCacheDataSource = dependencyContainer.recipeCacheDataSource
        recipeNetworkDataSource = dependencyContainer.recipeNetworkDataSource
        recipeFactory = dependencyContainer.recipeFactory
        syncDeletedRecipes = SyncDeletedRecipes(
            recipeCacheDataSource = recipeCacheDataSource,
            recipeNetworkDataSource = recipeNetworkDataSource
        )
    }

    @Test
    fun deleteNetworkRecipes_confirmCacheSync() = runBlocking {

        // select some recipes to be deleted from cache
        val networkRecipes = recipeNetworkDataSource.getAllRecipes()
        val recipesToDelete: ArrayList<Recipe> = ArrayList()
        for (recipe in networkRecipes) {
            recipesToDelete.add(recipe)
            recipeNetworkDataSource.deleteRecipe(recipe.id)
            if (recipesToDelete.size > 3) {
                break
            }
        }

        // perform sync
        syncDeletedRecipes.syncDeletedRecipes()

        // confirm recipes were deleted from cache
        for (recipe in recipesToDelete) {
            val cachedRecipe = recipeCacheDataSource.searchRecipeById(recipe.id)
            assertTrue { cachedRecipe == null }
        }
    }
}
