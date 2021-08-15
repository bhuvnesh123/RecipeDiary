package com.bhuvnesh.diary.business.interactors.recipeInsertNew

import com.bhuvnesh.diary.business.data.cache.CacheResponseHandler
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.data.utils.safeApiCall
import com.bhuvnesh.diary.business.data.utils.safeCacheCall
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.state.*
import com.bhuvnesh.diary.framework.presentation.recipeNew.state.RecipeInsertNewViewState
import com.bhuvnesh.diary.util.printLogD
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class InsertRecipe(
    private val recipeCacheDataSource: RecipeCacheDataSource,
    private val recipeNetworkDataSource: RecipeNetworkDataSource,
    private val recipeFactory: RecipeFactory
) {

    fun insertNewRecipe(
        id: String? = null,
        title: String,
        ingredients: String?,
        steps: String,
        imageUrl: String?,
        stateEvent: StateEvent
    ): Flow<DataState<RecipeInsertNewViewState>?> = flow {

        val newRecipe = recipeFactory.createSingleRecipe(
            id = id ?: UUID.randomUUID().toString(),
            title = title,
            ingredients = ingredients,
            steps = steps,
            imageUrl = imageUrl
        )
        val cacheResult = safeCacheCall(IO) {
            recipeCacheDataSource.insertRecipe(newRecipe)
        }
        val cacheResponse = object :
            CacheResponseHandler<RecipeInsertNewViewState, Long>(cacheResult, stateEvent) {
            override suspend fun handleSuccess(resultObj: Long): DataState<RecipeInsertNewViewState>? {
                if (resultObj > 0) {
                    printLogD("InsertRecipe", resultObj.toString())
                    val viewState =
                        RecipeInsertNewViewState(
                            newRecipe = newRecipe
                        )
                    return DataState.data(
                        response = Response(
                            message = INSERT_RECIPE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = viewState,
                        stateEvent = stateEvent
                    )
                } else {

                    printLogD("InsertRecipe", "Failed")
                    return DataState.data(
                        response = Response(
                            message = INSERT_RECIPE_FAILED,
                            uiComponentType = UIComponentType.SnackBar(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }
        }.getResult()

        printLogD("InsertRecipe", cacheResponse.toString())
        emit(cacheResponse)

        updateNetwork(cacheResponse?.stateMessage?.response?.message, newRecipe)
    }

    private suspend fun updateNetwork(cacheResponse: String?, newRecipe: Recipe) {
        if (cacheResponse.equals(INSERT_RECIPE_SUCCESS)) {

            safeApiCall(IO) { recipeNetworkDataSource.insertOrUpdateRecipe(newRecipe) }
        }
    }

    companion object {
        val INSERT_RECIPE_SUCCESS = "Successfully inserted new recipe."
        val INSERT_RECIPE_FAILED = "Failed to insert new recipe."
    }
}

