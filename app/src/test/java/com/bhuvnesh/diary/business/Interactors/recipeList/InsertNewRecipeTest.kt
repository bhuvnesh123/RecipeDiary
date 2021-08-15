package com.bhuvnesh.diary.business.Interactors.recipeList

import com.bhuvnesh.diary.business.data.cache.CacheErrors
import com.bhuvnesh.diary.business.data.cache.FORCE_GENERAL_FAILURE
import com.bhuvnesh.diary.business.data.cache.FORCE_NEW_RECIPE_EXCEPTION
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.state.DataState
import com.bhuvnesh.diary.business.interactors.recipeInsertNew.InsertRecipe
import com.bhuvnesh.diary.business.interactors.recipeInsertNew.InsertRecipe.Companion.INSERT_RECIPE_FAILED
import com.bhuvnesh.diary.business.interactors.recipeInsertNew.InsertRecipe.Companion.INSERT_RECIPE_SUCCESS
import com.bhuvnesh.diary.di.DependencyContainer
import com.bhuvnesh.diary.framework.presentation.recipeNew.state.RecipeInsertNewStateEvent
import com.bhuvnesh.diary.framework.presentation.recipeNew.state.RecipeInsertNewViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

@InternalCoroutinesApi
class InsertNewRecipeTest {

    // system in test
    private val insertNewRecipe: InsertRecipe

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
        insertNewRecipe = InsertRecipe(
            recipeCacheDataSource = recipeCacheDataSource,
            recipeNetworkDataSource = recipeNetworkDataSource,
            recipeFactory = recipeFactory
        )
    }

    @Test
    fun insertRecipe_success_confirmNetworkAndCacheUpdated() = runBlocking {
        val newRecipe = recipeFactory.createSingleRecipe(null, "Title", "Flour", "Steps", null)
        insertNewRecipe.insertNewRecipe(
            newRecipe.id,
            newRecipe.title,
            newRecipe.ingredients,
            newRecipe.steps,
            null,
            RecipeInsertNewStateEvent.InsertNewRecipeEvent(
                newRecipe.id,
                newRecipe.title,
                newRecipe.ingredients,
                newRecipe.steps
            )
        )
            .collect(object : FlowCollector<DataState<RecipeInsertNewViewState>?> {
                override suspend fun emit(value: DataState<RecipeInsertNewViewState>?) {
                    assertEquals(
                        value?.stateMessage?.response?.message,
                        INSERT_RECIPE_SUCCESS

                    )
                }
            })

        // confirm network was updated
        val networkRecipeThatWasInserted = recipeNetworkDataSource.searchRecipe(newRecipe)
        assertTrue { networkRecipeThatWasInserted == newRecipe }

        // confirm cache was updated
        val cacheRecipeThatWasInserted = recipeCacheDataSource.searchRecipeById(newRecipe.id)
        assertTrue { cacheRecipeThatWasInserted == newRecipe }

    }

    @Test
    fun insertRecipe_fail_confirmNetworkAndCacheUnchanged() = runBlocking {

        val newRecipe =
            recipeFactory.createSingleRecipe(FORCE_GENERAL_FAILURE, "Title", "Flour", "Steps", null)


        insertNewRecipe.insertNewRecipe(
            newRecipe.id,
            newRecipe.title,
            newRecipe.ingredients,
            newRecipe.steps,
            null,
            RecipeInsertNewStateEvent.InsertNewRecipeEvent(
                newRecipe.id,
                newRecipe.title,
                newRecipe.ingredients,
                newRecipe.steps
            )
        )
            .collect(object : FlowCollector<DataState<RecipeInsertNewViewState>?> {
                override suspend fun emit(value: DataState<RecipeInsertNewViewState>?) {
                    assertEquals(
                        value?.stateMessage?.response?.message,
                        INSERT_RECIPE_FAILED

                    )
                }
            })

        // confirm network was updated
        val networkRecipeThatWasInserted = recipeNetworkDataSource.searchRecipe(newRecipe)
        assertTrue { networkRecipeThatWasInserted == null }

        // confirm cache was updated
        val cacheRecipeThatWasInserted = recipeCacheDataSource.searchRecipeById(newRecipe.id)
        assertTrue { cacheRecipeThatWasInserted == null }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {
        val newRecipe = recipeFactory.createSingleRecipe(
            FORCE_NEW_RECIPE_EXCEPTION,
            "Title",
            "Flour",
            "Steps",
            null
        )


        insertNewRecipe.insertNewRecipe(
            newRecipe.id,
            newRecipe.title,
            newRecipe.ingredients,
            newRecipe.steps,
            null,
            RecipeInsertNewStateEvent.InsertNewRecipeEvent(
                newRecipe.id,
                newRecipe.title,
                newRecipe.ingredients,
                newRecipe.steps
            )
        )
            .collect(object : FlowCollector<DataState<RecipeInsertNewViewState>?> {
                override suspend fun emit(value: DataState<RecipeInsertNewViewState>?) {
                    assert(
                        value?.stateMessage?.response?.message
                            ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false

                    )
                }
            })

        // confirm network was updated
        val networkRecipeThatWasInserted = recipeNetworkDataSource.searchRecipe(newRecipe)
        assertTrue { networkRecipeThatWasInserted == null }

        // confirm cache was updated
        val cacheRecipeThatWasInserted = recipeCacheDataSource.searchRecipeById(newRecipe.id)
        assertTrue { cacheRecipeThatWasInserted == null }
    }

}
