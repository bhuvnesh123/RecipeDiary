package com.bhuvnesh.diary.business.Interactors.recipeList

import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.state.DataState
import com.bhuvnesh.diary.business.interactors.recipelist.GetNumRecipes
import com.bhuvnesh.diary.business.interactors.recipelist.GetNumRecipes.Companion.GET_NUM_RECIPES_SUCCESS
import com.bhuvnesh.diary.di.DependencyContainer
import com.bhuvnesh.diary.framework.presentation.recipeList.state.GetNumRecipesInCacheEvent
import com.bhuvnesh.diary.framework.presentation.recipeList.state.RecipeListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. getNumRecipes_success_confirmCorrect()
    a) get the number of recipes in cache
    b) listen for GET_NUM_RECIPES_SUCCESS from flow emission
    c) compare with the number of recipes in the fake data set
*/
@InternalCoroutinesApi
class GetNumRecipesTest {

    // system in test
    private val getNumRecipes: GetNumRecipes

    // dependencies
    private val dependencyContainer: DependencyContainer
    private val recipeCacheDataSource: RecipeCacheDataSource
    private val recipeFactory: RecipeFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        recipeCacheDataSource = dependencyContainer.recipeCacheDataSource
        recipeFactory = dependencyContainer.recipeFactory
        getNumRecipes = GetNumRecipes(
            recipeCacheDataSource = recipeCacheDataSource
        )
    }


    @Test
    fun getNumRecipes_success_confirmCorrect() = runBlocking {

        var numRecipes = 0
        getNumRecipes.getNumRecipes(
            stateEvent = GetNumRecipesInCacheEvent
        ).collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    GET_NUM_RECIPES_SUCCESS
                )
                numRecipes = value?.data?.numRecipesInCache ?: 0
            }
        })

        val actualNumRecipesInCache = recipeCacheDataSource.getNumRecipes()
        assertTrue { actualNumRecipesInCache == numRecipes }
    }


}