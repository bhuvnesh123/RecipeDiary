package com.bhuvnesh.diary.business.Interactors.recipeDetail


import com.bhuvnesh.diary.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.bhuvnesh.diary.business.data.cache.FORCE_UPDATE_RECIPE_EXCEPTION
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.state.DataState
import com.bhuvnesh.diary.business.interactors.recipedetail.UpdateRecipe
import com.bhuvnesh.diary.business.interactors.recipedetail.UpdateRecipe.Companion.UPDATE_RECIPE_FAILED
import com.bhuvnesh.diary.business.interactors.recipedetail.UpdateRecipe.Companion.UPDATE_RECIPE_SUCCESS
import com.bhuvnesh.diary.di.DependencyContainer
import com.bhuvnesh.diary.framework.presentation.recipeDetail.state.RecipeDetailStateEvent
import com.bhuvnesh.diary.framework.presentation.recipeDetail.state.RecipeDetailViewState
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*


/*
Test cases:
1. updateRecipe_success_confirmNetworkAndCacheUpdated()
    a) select a random recipe from the cache
    b) update that recipe
    c) confirm UPDATE_RECIPE_SUCCESS msg is emitted from flow
    d) confirm recipe is updated in network
    e) confirm recipe is updated in cache
2. updateRecipe_fail_confirmNetworkAndCacheUnchanged()
    a) attempt to update a recipe, fail since does not exist
    b) check for failure message from flow emission
    c) confirm nothing was updated in the cache
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) attempt to update a recipe, force an exception to throw
    b) check for failure message from flow emission
    c) confirm nothing was updated in the cache
 */
@InternalCoroutinesApi
class UpdateRecipeTest {

    // system in test
    private val updateRecipe: UpdateRecipe

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
        updateRecipe = UpdateRecipe(
            recipeCacheDataSource = recipeCacheDataSource,
            recipeNetworkDataSource = recipeNetworkDataSource
        )
    }

    @Test
    fun updateRecipe_success_confirmNetworkAndCacheUpdated() = runBlocking {

        val randomRecipe = recipeCacheDataSource.searchRecipes("", "", 1)
            .get(0)
        val updatedRecipe = recipeFactory.createSingleRecipe(
            id = randomRecipe.id,
            title = UUID.randomUUID().toString(),
            ingredients = UUID.randomUUID().toString(),
            steps = UUID.randomUUID().toString(),
            imageUrl = UUID.randomUUID().toString()
        )
        updateRecipe.updateRecipe(
            recipe = updatedRecipe,
            stateEvent = RecipeDetailStateEvent.UpdateRecipeEvent
        ).collect(object : FlowCollector<DataState<RecipeDetailViewState>?> {
            override suspend fun emit(value: DataState<RecipeDetailViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    UPDATE_RECIPE_SUCCESS
                )
            }
        })

        // confirm cache was updated
        val cacheRecipe = recipeCacheDataSource.searchRecipeById(updatedRecipe.id)
        assertTrue { cacheRecipe == updatedRecipe }

        // confirm that network was updated
        val networkRecipe = recipeNetworkDataSource.searchRecipe(updatedRecipe)
        assertTrue { networkRecipe == updatedRecipe }
    }

    @Test
    fun updateRecipe_fail_confirmNetworkAndCacheUnchanged() = runBlocking {

        // create a recipe that doesnt exist in cache
        val recipeToUpdate = Recipe(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            ingredients = UUID.randomUUID().toString(),
            steps = UUID.randomUUID().toString(),
            imageUrl = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )
        updateRecipe.updateRecipe(
            recipe = recipeToUpdate,
            stateEvent = RecipeDetailStateEvent.UpdateRecipeEvent
        ).collect(object : FlowCollector<DataState<RecipeDetailViewState>?> {
            override suspend fun emit(value: DataState<RecipeDetailViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    UPDATE_RECIPE_FAILED
                )
            }
        })

        // confirm nothing updated in cache
        val cacheRecipe = recipeCacheDataSource.searchRecipeById(recipeToUpdate.id)
        assertTrue { cacheRecipe == null }

        // confirm nothing updated in network
        val networkRecipe = recipeNetworkDataSource.searchRecipe(recipeToUpdate)
        assertTrue { networkRecipe == null }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {

        // create a recipe that doesnt exist in cache
        val recipeToUpdate = Recipe(
            id = FORCE_UPDATE_RECIPE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            ingredients = UUID.randomUUID().toString(),
            steps = UUID.randomUUID().toString(),
            imageUrl = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )
        updateRecipe.updateRecipe(
            recipe = recipeToUpdate,
            stateEvent = RecipeDetailStateEvent.UpdateRecipeEvent
        ).collect(object : FlowCollector<DataState<RecipeDetailViewState>?> {
            override suspend fun emit(value: DataState<RecipeDetailViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        // confirm nothing updated in cache
        val cacheRecipe = recipeCacheDataSource.searchRecipeById(recipeToUpdate.id)
        assertTrue { cacheRecipe == null }

        // confirm nothing updated in network
        val networkRecipe = recipeNetworkDataSource.searchRecipe(recipeToUpdate)
        assertTrue { networkRecipe == null }
    }
}
