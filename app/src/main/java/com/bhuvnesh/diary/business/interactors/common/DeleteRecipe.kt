package com.bhuvnesh.diary.business.interactors.recipelist

import com.bhuvnesh.diary.business.data.cache.CacheResponseHandler
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.data.utils.safeApiCall
import com.bhuvnesh.diary.business.data.utils.safeCacheCall
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteRecipe<ViewState>(
    private val recipeCacheDataSource: RecipeCacheDataSource,
    private val recipeNetworkDataSource: RecipeNetworkDataSource
) {

    fun deleteRecipe(
        recipe: Recipe,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            recipeCacheDataSource.deleteRecipe(recipe.id)
        }

        val response = object : CacheResponseHandler<ViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<ViewState>? {
                return if (resultObj > 0) {
                    DataState.data(
                        response = Response(
                            message = DELETE_RECIPE_SUCCESS,
                            uiComponentType = UIComponentType.SnackBar(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                } else {
                    DataState.data(
                        response = Response(
                            message = DELETE_RECIPE_FAILED,
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

        // update network
        if (response?.stateMessage?.response?.message.equals(DELETE_RECIPE_SUCCESS)) {

            // delete from 'recipes' node
            safeApiCall(IO) {
                recipeNetworkDataSource.deleteRecipe(recipe.id)
            }

            // insert into 'deletes' node
            safeApiCall(IO) {
                recipeNetworkDataSource.insertDeletedRecipe(recipe)
            }

        }
    }

    companion object {
        val DELETE_RECIPE_SUCCESS = "Successfully deleted recipe."
        val DELETE_RECIPE_FAILED = "Failed to delete recipe."
    }
}