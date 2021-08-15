package com.bhuvnesh.diary.business.Interactors.splash


import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.interactors.splash.SyncRecipes
import com.bhuvnesh.diary.di.DependencyContainer
import com.bhuvnesh.diary.framework.dataSource.cache.database.ORDER_BY_ASC_DATE_UPDATED
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList


/*
Test cases:
1. insertNetworkRecipesIntoCache()
    a) insert a bunch of new recipes into the cache
    b) perform the sync
    c) check to see that those recipes were inserted into the network
2. insertCachedRecipesIntoNetwork()
    a) insert a bunch of new recipes into the network
    b) perform the sync
    c) check to see that those recipes were inserted into the cache
3. checkCacheUpdateLogicSync()
    a) select some recipes from the cache and update them
    b) perform sync
    c) confirm network reflects the updates
4. checkNetworkUpdateLogicSync()
    a) select some recipes from the network and update them
    b) perform sync
    c) confirm cache reflects the updates
 */

@InternalCoroutinesApi
class SyncRecipesTest {

    // system in test
    private val syncRecipes: SyncRecipes

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
        syncRecipes = SyncRecipes(
            recipeCacheDataSource = recipeCacheDataSource,
            recipeNetworkDataSource = recipeNetworkDataSource
        )
    }

    @Test
    fun insertNetworkRecipesIntoCache() = runBlocking {

        // prepare the scenario
        // -> Recipes in network are newer so they must be inserted into cache
        val newRecipes = recipeFactory.createRecipeList(50)
        recipeNetworkDataSource.insertOrUpdateRecipes(newRecipes)

        // perform the sync
        syncRecipes.syncRecipes()

        // confirm the new recipes were inserted into cache
        for (recipe in newRecipes) {
            val cachedRecipe = recipeCacheDataSource.searchRecipeById(recipe.id)
            assertTrue { cachedRecipe != null }
        }
    }


    @Test
    fun insertCachedRecipesIntoNetwork() = runBlocking {

        // prepare the scenario
        // -> Recipes in cache are newer so they must be inserted into network
        val newRecipes = recipeFactory.createRecipeList(50)
        recipeCacheDataSource.insertRecipes(newRecipes)

        // perform the sync
        syncRecipes.syncRecipes()

        // confirm the new recipes were inserted into network
        for (recipe in newRecipes) {
            val networkRecipe = recipeNetworkDataSource.searchRecipe(recipe)
            assertTrue { networkRecipe != null }
        }
    }

    @Test
    fun checkCacheUpdateLogicSync() = runBlocking {

        // select a few recipes from cache and update the title and body
        val cachedRecipes = recipeCacheDataSource.searchRecipes(
            query = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        val recipesToUpdate: ArrayList<Recipe> = ArrayList()
        for (recipe in cachedRecipes) {
            val updatedRecipe = recipeFactory.createSingleRecipe(
                id = recipe.id,
                title = UUID.randomUUID().toString(),
                ingredients = UUID.randomUUID().toString(),
                steps = UUID.randomUUID().toString(),
                imageUrl = UUID.randomUUID().toString()
            )
            recipesToUpdate.add(updatedRecipe)
            if (recipesToUpdate.size > 3) {
                break
            }
        }
        recipeCacheDataSource.insertRecipes(recipesToUpdate)

        // perform sync
        syncRecipes.syncRecipes()

        // confirm the updated recipes were updated in the network
        for (recipe in recipesToUpdate) {
            val networkRecipe = recipeNetworkDataSource.searchRecipe(recipe)
            assertEquals(recipe.id, networkRecipe?.id)
            assertEquals(recipe.title, networkRecipe?.title)
            assertEquals(recipe.ingredients, networkRecipe?.ingredients)
            assertEquals(recipe.updated_at, networkRecipe?.updated_at)
        }
    }

    @Test
    fun checkNetworkUpdateLogicSync() = runBlocking {

        // select a few recipes from network and update the title and body
        val networkRecipes = recipeNetworkDataSource.getAllRecipes()

        val recipesToUpdate: ArrayList<Recipe> = ArrayList()
        for (recipe in networkRecipes) {
            val updatedRecipe = recipeFactory.createSingleRecipe(
                id = recipe.id,
                title = UUID.randomUUID().toString(),
                ingredients = UUID.randomUUID().toString(),
                steps = UUID.randomUUID().toString(),
                imageUrl = UUID.randomUUID().toString()
            )
            recipesToUpdate.add(updatedRecipe)
            if (recipesToUpdate.size > 3) {
                break
            }
        }
        recipeNetworkDataSource.insertOrUpdateRecipes(recipesToUpdate)

        // perform sync
        syncRecipes.syncRecipes()

        // confirm the updated recipes were updated in the cache
        for (recipe in recipesToUpdate) {
            val cacheRecipe = recipeCacheDataSource.searchRecipeById(recipe.id)
            assertEquals(recipe.id, cacheRecipe?.id)
            assertEquals(recipe.title, cacheRecipe?.title)
            assertEquals(recipe.ingredients, cacheRecipe?.ingredients)
            assertEquals(recipe.updated_at, cacheRecipe?.updated_at)
        }
    }
}
