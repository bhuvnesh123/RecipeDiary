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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
    Query all recipes in the cache. It will then search firestore for
    each corresponding recipe but with an extra filter: It will only return recipes where
    cached_recipe.updated_at < network_recipe.updated_at. It will update the cached recipes
    where that condition is met. If the recipe does not exist in Firestore (maybe due to
    network being down at time of insertion), insert it
    (**This must be done AFTER
    checking for deleted recipes and performing that sync**).
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
class SyncRecipes(
    private val recipeCacheDataSource: RecipeCacheDataSource,
    private val recipeNetworkDataSource: RecipeNetworkDataSource
) {

    suspend fun syncRecipes() {

        val cachedRecipesList = getCachedRecipes()

        syncNetworkRecipesWithCachedRecipes(ArrayList(cachedRecipesList))
    }

    private suspend fun getCachedRecipes(): List<Recipe> {
        val cacheResult = safeCacheCall(IO) {
            recipeCacheDataSource.getAllRecipes()
        }

        val response = object : CacheResponseHandler<List<Recipe>, List<Recipe>>(
            response = cacheResult,
            stateEvent = null
        ) {
            override suspend fun handleSuccess(resultObj: List<Recipe>): DataState<List<Recipe>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }

        }.getResult()

        return response?.data ?: ArrayList()
    }

    // get all recipes from network
    // if they do not exist in cache, insert them
    // if they do exist in cache, make sure they are up to date
    // while looping, remove recipes from the cachedRecipes list. If any remain, it means they
    // should be in the network but aren't. So insert them.
    private suspend fun syncNetworkRecipesWithCachedRecipes(
        cachedRecipes: ArrayList<Recipe>
    ) = withContext(IO) {

        val networkResult = safeApiCall(IO) {
            recipeNetworkDataSource.getAllRecipes()
        }

        val response = object : ApiResponseHandler<List<Recipe>, List<Recipe>>(
            response = networkResult,
            stateEvent = null
        ) {
            override suspend fun handleSuccess(resultObj: List<Recipe>): DataState<List<Recipe>>? {
                return DataState.data(
                    response = null,
                    data = resultObj,
                    stateEvent = null
                )
            }
        }.getResult()

        val recipeList = response?.data ?: ArrayList()

        val job = launch {
            for (recipe in recipeList) {
                recipeCacheDataSource.searchRecipeById(recipe.id)?.let { cachedRecipe ->
                    cachedRecipes.remove(cachedRecipe)
                    checkIfCachedRecipeRequiresUpdate(cachedRecipe, recipe)
                } ?: recipeCacheDataSource.insertRecipe(recipe)
            }
        }
        job.join()

        // insert remaining into network
        for (cachedRecipe in cachedRecipes) {
            recipeNetworkDataSource.insertOrUpdateRecipe(cachedRecipe)
        }
    }

    private suspend fun checkIfCachedRecipeRequiresUpdate(
        cachedRecipe: Recipe,
        networkRecipe: Recipe
    ) {
        val cacheUpdatedAt = cachedRecipe.updated_at
        val networkUpdatedAt = networkRecipe.updated_at

        // update cache (network has newest data)
        if (networkUpdatedAt > cacheUpdatedAt) {
            printLogD(
                "SyncRecipes",
                "cacheUpdatedAt: ${cacheUpdatedAt}, " +
                        "networkUpdatedAt: ${networkUpdatedAt}, " +
                        "recipe: ${cachedRecipe.title}"
            )
            safeCacheCall(IO) {
                recipeCacheDataSource.updateRecipe(
                    networkRecipe.id,
                    networkRecipe.title,
                    networkRecipe.ingredients,
                    networkRecipe.steps,
                    networkRecipe.imageUrl,
                    null
                )
            }
        }
        // update network (cache has newest data)
        else {
            safeApiCall(IO) {
                recipeNetworkDataSource.insertOrUpdateRecipe(cachedRecipe)
            }
        }
    }

    // for debugging
//    private fun printCacheLongTimestamps(recipes: List<Recipe>){
//        for(recipe in recipes){
//            printLogD("SyncRecipes",
//                "date: ${dateUtil.convertServerStringDateToLong(recipe.updated_at)}")
//        }
//    }

}
