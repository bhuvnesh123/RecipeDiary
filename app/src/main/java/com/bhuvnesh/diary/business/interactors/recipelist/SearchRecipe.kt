package com.bhuvnesh.diary.business.interactors.recipelist

import com.bhuvnesh.diary.business.data.cache.CacheResponseHandler
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.utils.safeCacheCall
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.*
import com.bhuvnesh.diary.framework.presentation.recipeList.state.RecipeListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRecipes(
    private val recipeCacheDataSource: RecipeCacheDataSource
) {

    fun searchRecipes(
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<RecipeListViewState>?> = flow {

        var updatedPage = page
        if (page <= 0) {
            updatedPage = 1
        }
        val cacheResult = safeCacheCall(Dispatchers.IO) {
            recipeCacheDataSource.searchRecipes(
                query = query,
                filterAndOrder = filterAndOrder,
                page = updatedPage
            )
        }

        val response = object : CacheResponseHandler<RecipeListViewState, List<Recipe>>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: List<Recipe>): DataState<RecipeListViewState>? {
                var message: String? =
                    SEARCH_RECIPES_SUCCESS
                var uiComponentType: UIComponentType? = UIComponentType.None()
                if (resultObj.size == 0) {
                    message =
                        SEARCH_RECIPES_NO_MATCHING_RESULTS
                    uiComponentType = UIComponentType.Toast()
                }
                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType as UIComponentType,
                        messageType = MessageType.Success()
                    ),
                    data = RecipeListViewState(
                        recipeList = ArrayList(resultObj)
                    ),
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(response)
    }

    companion object {
        val SEARCH_RECIPES_SUCCESS = "Successfully retrieved list of recipes."
        val SEARCH_RECIPES_NO_MATCHING_RESULTS = "No recipes found."
        val SEARCH_RECIPES_FAILED = "Failed to retrieve the list of recipes."

    }
}