package com.bhuvnesh.diary.business.Interactors.common

import com.bhuvnesh.diary.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.bhuvnesh.diary.business.data.cache.FORCE_DELETE_RECIPE_EXCEPTION
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.state.DataState
import com.bhuvnesh.diary.business.interactors.recipelist.DeleteRecipe
import com.bhuvnesh.diary.business.interactors.recipelist.DeleteRecipe.Companion.DELETE_RECIPE_FAILED
import com.bhuvnesh.diary.business.interactors.recipelist.DeleteRecipe.Companion.DELETE_RECIPE_SUCCESS
import com.bhuvnesh.diary.di.DependencyContainer
import com.bhuvnesh.diary.framework.presentation.recipeList.state.RecipeListStateEvent
import com.bhuvnesh.diary.framework.presentation.recipeList.state.RecipeListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*


/*
Test cases:
1. deleteRecipe_success_confirmNetworkUpdated()
    a) delete a recipe
    b) check for success message from flow emission
    c) confirm recipe was deleted from "recipes" node in network
    d) confirm recipe was added to "deletes" node in network
2. deleteRecipe_fail_confirmNetworkUnchanged()
    a) attempt to delete a recipe, fail since does not exist
    b) check for failure message from flow emission
    c) confirm network was not changed
3. throwException_checkGenericError_confirmNetworkUnchanged()
    a) attempt to delete a recipe, force an exception to throw
    b) check for failure message from flow emission
    c) confirm network was not changed
 */
@InternalCoroutinesApi
class DeleteRecipesTest {

    // system in test
    private val deleteRecipes: DeleteRecipe<RecipeListViewState>

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
        deleteRecipes = DeleteRecipe(
            recipeCacheDataSource = recipeCacheDataSource,
            recipeNetworkDataSource = recipeNetworkDataSource
        )
    }

    @Test
    fun deleteRecipe_success_confirmNetworkUpdated() = runBlocking {

        // choose a recipe at random to delete
        // select a random recipe to update
        val recipeToDelete = recipeCacheDataSource
            .searchRecipes("", "", 1).get(0)

        deleteRecipes.deleteRecipe(
            recipeToDelete,
            RecipeListStateEvent.DeleteRecipeEvent(recipeToDelete)
        ).collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_RECIPE_SUCCESS
                )
            }
        })

        // confirm was deleted from "recipes" node
        val wasRecipeDeleted = !recipeNetworkDataSource.getAllRecipes()
            .contains(recipeToDelete)
        assertTrue { wasRecipeDeleted }

        // confirm was inserted into "deletes" node
        val wasDeletedRecipeInserted = recipeNetworkDataSource.getDeletedRecipes()
            .contains(recipeToDelete)
        assertTrue { wasDeletedRecipeInserted }
    }

    @Test
    fun deleteRecipe_fail_confirmNetworkUnchanged() = runBlocking {

        // create a recipe to delete that doesn't exist in data set
        val recipeToDelete = Recipe(
            id = UUID.randomUUID().toString(),
            title = UUID.randomUUID().toString(),
            ingredients = UUID.randomUUID().toString(),
            steps = UUID.randomUUID().toString(),
            imageUrl = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )


        deleteRecipes.deleteRecipe(
            recipeToDelete,
            RecipeListStateEvent.DeleteRecipeEvent(recipeToDelete)
        ).collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_RECIPE_FAILED
                )
            }
        })

        // confirm nothing was deleted from "recipes" node
        val recipes = recipeNetworkDataSource.getAllRecipes()
        val numRecipesInCache = recipeCacheDataSource.getNumRecipes()
        assertTrue { numRecipesInCache == recipes.size }

        // confirm was NOT inserted into "deletes" node
        val wasDeletedRecipeInserted = !recipeNetworkDataSource.getDeletedRecipes()
            .contains(recipeToDelete)
        assertTrue { wasDeletedRecipeInserted }
    }

    @Test
    fun throwException_checkGenericError_confirmNetworkUnchanged() = runBlocking {

        // create a recipe to delete that will throw exception
        val recipeToDelete = Recipe(
            id = FORCE_DELETE_RECIPE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            ingredients = UUID.randomUUID().toString(),
            steps = UUID.randomUUID().toString(),
            imageUrl = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )


        deleteRecipes.deleteRecipe(
            recipeToDelete,
            RecipeListStateEvent.DeleteRecipeEvent(recipeToDelete)
        ).collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        // confirm nothing was deleted from "recipes" node
        val recipes = recipeNetworkDataSource.getAllRecipes()
        val numRecipesInCache = recipeCacheDataSource.getNumRecipes()
        assertTrue { numRecipesInCache == recipes.size }

        // confirm was NOT inserted into "deletes" node
        val wasDeletedRecipeInserted = !recipeNetworkDataSource.getDeletedRecipes()
            .contains(recipeToDelete)
        assertTrue { wasDeletedRecipeInserted }
    }

}