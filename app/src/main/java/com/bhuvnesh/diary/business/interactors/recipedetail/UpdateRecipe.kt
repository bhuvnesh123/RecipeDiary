package com.bhuvnesh.diary.business.interactors.recipedetail

import com.bhuvnesh.diary.business.data.cache.CacheResponseHandler
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.data.utils.safeApiCall
import com.bhuvnesh.diary.business.data.utils.safeCacheCall
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.*
import com.bhuvnesh.diary.framework.presentation.recipeDetail.state.RecipeDetailViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateRecipe(
    private val recipeCacheDataSource: RecipeCacheDataSource,
    private val recipeNetworkDataSource: RecipeNetworkDataSource
) {

    fun updateRecipe(
        recipe: Recipe,
        stateEvent: StateEvent
    ): Flow<DataState<RecipeDetailViewState>?> = flow {

        val cacheResult = safeCacheCall(Dispatchers.IO) {
            recipeCacheDataSource.updateRecipe(
                primaryKey = recipe.id,
                newTitle = recipe.title,
                newIngredients = recipe.ingredients,
                newSteps = recipe.steps,
                newImageUrl = recipe.imageUrl,
                newUpdatedAt = null
            )
        }

        val response = object : CacheResponseHandler<RecipeDetailViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<RecipeDetailViewState>? {
                return if (resultObj > 0) {
                    DataState.data(
                        response = Response(
                            message = UPDATE_RECIPE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = UPDATE_RECIPE_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        emit(response)

        updateNetwork(response?.stateMessage?.response?.message, recipe)
    }

    private suspend fun updateNetwork(response: String?, recipe: Recipe) {
        if (response.equals(UPDATE_RECIPE_SUCCESS)) {

            safeApiCall(Dispatchers.IO) {
                recipeNetworkDataSource.insertOrUpdateRecipe(recipe)
            }
        }
    }

    companion object {
        val UPDATE_RECIPE_SUCCESS = "Successfully updated recipe."
        val UPDATE_RECIPE_FAILED = "Failed to update recipe."
        val UPDATE_RECIPE_FAILED_PK = "Update failed. Recipe is missing primary key."

    }
}
