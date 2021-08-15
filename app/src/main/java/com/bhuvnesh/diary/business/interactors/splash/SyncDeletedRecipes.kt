package com.bhuvnesh.diary.business.interactors.splash


import com.bhuvnesh.diary.business.data.cache.CacheResponseHandler
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.ApiResponseHandler
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.data.utils.safeApiCall
import com.bhuvnesh.diary.business.data.utils.safeCacheCall
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.DataState
import com.bhuvnesh.diary.util.printLogD
import kotlinx.coroutines.Dispatchers.IO

/*
    Search firestore for all recipes in the "deleted" node.
    It will then search the cache for recipes matching those deleted recipes.
    If a match is found, it is deleted from the cache.
 */
class SyncDeletedRecipes(
    private val recipeCacheDataSource: RecipeCacheDataSource,
    private val recipeNetworkDataSource: RecipeNetworkDataSource
) {

    suspend fun syncDeletedRecipes() {

        val apiResult = safeApiCall(IO) {
            recipeNetworkDataSource.getDeletedRecipes()
        }
        val response = object : ApiResponseHandler<List<Recipe>, List<Recipe>>(
            response = apiResult,
            stateEvent = null
        ) {
            override suspend fun handleSuccess(resultObj: List<Recipe>): DataState<List<Recipe>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }

        val recipes = response.getResult()?.data ?: ArrayList()

        val cacheResult = safeCacheCall(IO) {
            recipeCacheDataSource.deleteRecipes(recipes)
        }

        object : CacheResponseHandler<Int, Int>(
            response = cacheResult,
            stateEvent = null
        ) {
            override suspend fun handleSuccess(resultObj: Int): DataState<Int>? {
                printLogD(
                    "SyncRecipes",
                    "num deleted recipes: ${resultObj}"
                )
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }.getResult()

    }


}
