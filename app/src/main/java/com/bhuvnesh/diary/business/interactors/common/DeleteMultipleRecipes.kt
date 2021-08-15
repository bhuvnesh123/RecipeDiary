package com.bhuvnesh.diary.business.interactors.common

import com.bhuvnesh.diary.business.data.cache.CacheResponseHandler
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.data.utils.safeApiCall
import com.bhuvnesh.diary.business.data.utils.safeCacheCall
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.*
import com.bhuvnesh.diary.framework.presentation.recipeList.state.RecipeListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteMultipleRecipes(
    private val recipeCacheDataSource: RecipeCacheDataSource,
    private val recipeNetworkDataSource: RecipeNetworkDataSource
) {

    // set true if an error occurs when deleting any of the recipes from cache
    private var onDeleteError: Boolean = false

    /**
     * Logic:
     * 1. execute all the deletes and save result into an ArrayList<DataState<RecipeListViewState>>
     * 2a. If one of the results is a failure, emit an "error" response
     * 2b. If all success, emit success response
     * 3. Update network with recipes that were successfully deleted
     */
    fun deleteRecipes(
        recipes: List<Recipe>,
        stateEvent: StateEvent
    ): Flow<DataState<RecipeListViewState>?> = flow {

        val successfulDeletes: ArrayList<Recipe> =
            ArrayList() // recipes that were successfully deleted
        for (recipe in recipes) {
            val cacheResult = safeCacheCall(IO) {
                recipeCacheDataSource.deleteRecipe(recipe.id)
            }

            val response = object : CacheResponseHandler<RecipeListViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: Int): DataState<RecipeListViewState>? {
                    if (resultObj < 0) { // if error
                        onDeleteError = true
                    } else {
                        successfulDeletes.add(recipe)
                    }
                    return null
                }
            }.getResult()

            // check for random errors
            if (response?.stateMessage?.response?.message
                    ?.contains(stateEvent.errorInfo()) == true
            ) {
                onDeleteError = true
            }

        }

        if (onDeleteError) {
            emit(
                DataState.data<RecipeListViewState>(
                    response = Response(
                        message = DELETE_RECIPES_ERRORS,
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        } else {
            emit(
                DataState.data<RecipeListViewState>(
                    response = Response(
                        message = DELETE_RECIPES_SUCCESS,
                        uiComponentType = UIComponentType.Toast(),
                        messageType = MessageType.Success()
                    ),
                    data = null,
                    stateEvent = stateEvent
                )
            )
        }

        updateNetwork(successfulDeletes)
    }

    private suspend fun updateNetwork(successfulDeletes: ArrayList<Recipe>) {
        for (recipe in successfulDeletes) {

            // delete from "recipes" node
            safeApiCall(IO) {
                recipeNetworkDataSource.deleteRecipe(recipe.id)
            }

            // insert into "deletes" node
            safeApiCall(IO) {
                recipeNetworkDataSource.insertDeletedRecipe(recipe)
            }
        }
    }

    companion object {
        val DELETE_RECIPES_SUCCESS = "Successfully deleted recipes."
        val DELETE_RECIPES_ERRORS =
            "Not all the recipes you selected were deleted. There was some errors."
        val DELETE_RECIPES_YOU_MUST_SELECT = "You haven't selected any recipes to delete."
        val DELETE_RECIPES_ARE_YOU_SURE = "Are you sure you want to delete these?"
    }
}
