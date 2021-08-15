package com.bhuvnesh.diary.business.Interactors.recipeList

import com.bhuvnesh.diary.business.data.cache.CacheErrors
import com.bhuvnesh.diary.business.data.cache.FORCE_SEARCH_RECIPES_EXCEPTION
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.state.DataState
import com.bhuvnesh.diary.business.interactors.recipelist.SearchRecipes
import com.bhuvnesh.diary.business.interactors.recipelist.SearchRecipes.Companion.SEARCH_RECIPES_NO_MATCHING_RESULTS
import com.bhuvnesh.diary.business.interactors.recipelist.SearchRecipes.Companion.SEARCH_RECIPES_SUCCESS
import com.bhuvnesh.diary.di.DependencyContainer
import com.bhuvnesh.diary.framework.dataSource.cache.database.ORDER_BY_ASC_DATE_UPDATED
import com.bhuvnesh.diary.framework.presentation.recipeList.state.RecipeListViewState
import com.bhuvnesh.diary.framework.presentation.recipeList.state.SearchRecipesEvent
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

/*
Test cases:
1. blankQuery_success_confirmRecipesRetrieved()
    a) query with some default search options
    b) listen for SEARCH_RECIPES_SUCCESS emitted from flow
    c) confirm recipes were retrieved
    d) confirm recipes in cache match with recipes that were retrieved
2. randomQuery_success_confirmNoResults()
    a) query with something that will yield no results
    b) listen for SEARCH_RECIPES_NO_MATCHING_RESULTS emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is recipes in the cache
3. searchRecipes_fail_confirmNoResults()
    a) force an exception to be thrown
    b) listen for CACHE_ERROR_UNKNOWN emitted from flow
    c) confirm nothing was retrieved
    d) confirm there is recipes in the cache
 */
@InternalCoroutinesApi
class SearchRecipesTest {

    // system in test
    private val searchRecipes: SearchRecipes

    // dependencies
    private val dependencyContainer: DependencyContainer
    private val recipeCacheDataSource: RecipeCacheDataSource
    private val recipeFactory: RecipeFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        recipeCacheDataSource = dependencyContainer.recipeCacheDataSource
        recipeFactory = dependencyContainer.recipeFactory
        searchRecipes = SearchRecipes(
            recipeCacheDataSource = recipeCacheDataSource
        )
    }

    @Test
    fun blankQuery_success_confirmRecipesRetrieved() = runBlocking {

        val query = ""
        var results: ArrayList<Recipe>? = null
        searchRecipes.searchRecipes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = SearchRecipesEvent()
        ).collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    SEARCH_RECIPES_SUCCESS
                )
                value?.data?.recipeList?.let { list ->
                    results = ArrayList(list)
                }

            }
        })

        // confirm recipes were retrieved
        assertTrue { results != null }

        // confirm recipes in cache match with recipes that were retrieved
        val recipesInCache = recipeCacheDataSource.searchRecipes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { results?.containsAll(recipesInCache) ?: false }
    }

    @Test
    fun randomQuery_success_confirmNoResults() = runBlocking {

        val query = "hthrthrgrkgenrogn843nn4u34n934v53454hrth"
        var results: ArrayList<Recipe>? = null
        searchRecipes.searchRecipes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = SearchRecipesEvent()
        ).collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    SEARCH_RECIPES_NO_MATCHING_RESULTS
                )
                value?.data?.recipeList?.let { list ->
                    results = ArrayList(list)
                }
            }
        })

        // confirm nothing was retrieved
        assertTrue { results?.run { size == 0 } ?: true }

        // confirm there is recipes in the cache
        val recipesInCache = recipeCacheDataSource.searchRecipes(
            query = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { recipesInCache.size > 0 }
    }

    @Test
    fun searchRecipes_fail_confirmNoResults() = runBlocking {

        val query = FORCE_SEARCH_RECIPES_EXCEPTION
        var results: ArrayList<Recipe>? = null
        searchRecipes.searchRecipes(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = SearchRecipesEvent()
        ).collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
                )
                value?.data?.recipeList?.let { list ->
                    results = ArrayList(list)
                }
                println("results: ${results}")
            }
        })

        // confirm nothing was retrieved
        assertTrue { results?.run { size == 0 } ?: true }

        // confirm there is recipes in the cache
        val recipesInCache = recipeCacheDataSource.searchRecipes(
            query = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { recipesInCache.size > 0 }
    }


}
