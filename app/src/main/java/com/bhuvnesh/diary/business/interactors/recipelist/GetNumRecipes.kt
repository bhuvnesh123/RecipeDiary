package com.bhuvnesh.diary.business.interactors.recipelist

import com.bhuvnesh.diary.business.data.cache.CacheResponseHandler
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.utils.safeCacheCall
import com.bhuvnesh.diary.business.domain.state.*
import com.bhuvnesh.diary.framework.presentation.recipeList.state.RecipeListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetNumRecipes(
    private val recipeCacheDataSource: RecipeCacheDataSource
) {

    fun getNumRecipes(
        stateEvent: StateEvent
    ): Flow<DataState<RecipeListViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            recipeCacheDataSource.getNumRecipes()
        }
        val response = object : CacheResponseHandler<RecipeListViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<RecipeListViewState>? {
                val viewState = RecipeListViewState(
                    numRecipesInCache = resultObj
                )
                return DataState.data(
                    response = Response(
                        message = GET_NUM_RECIPES_SUCCESS,
                        uiComponentType = UIComponentType.None(),
                        messageType = MessageType.Success()
                    ),
                    data = viewState,
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(response)
    }

    companion object {
        val GET_NUM_RECIPES_SUCCESS = "Successfully retrieved the number of recipes from the cache."
        val GET_NUM_RECIPES_FAILED = "Failed to get the number of recipes from the cache."
    }
}
