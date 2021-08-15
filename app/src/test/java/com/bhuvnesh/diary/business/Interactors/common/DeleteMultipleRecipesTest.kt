package com.bhuvnesh.diary.business.Interactors.common


import com.bhuvnesh.diary.business.data.cache.FORCE_DELETE_RECIPE_EXCEPTION
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.state.DataState
import com.bhuvnesh.diary.business.interactors.common.DeleteMultipleRecipes
import com.bhuvnesh.diary.business.interactors.common.DeleteMultipleRecipes.Companion.DELETE_RECIPES_ERRORS
import com.bhuvnesh.diary.business.interactors.common.DeleteMultipleRecipes.Companion.DELETE_RECIPES_SUCCESS
import com.bhuvnesh.diary.di.DependencyContainer
import com.bhuvnesh.diary.framework.presentation.recipeList.state.DeleteMultipleRecipesEvent
import com.bhuvnesh.diary.framework.presentation.recipeList.state.RecipeListViewState
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList


/*
Test cases:
1. deleteRecipes_success_confirmNetworkAndCacheUpdated()
    a) select a handful of random recipes for deleting
    b) delete from cache and network
    c) confirm DELETE_RECIPES_SUCCESS msg is emitted from flow
    d) confirm recipes are delted from cache
    e) confirm recipes are deleted from "recipes" node in network
    f) confirm recipes are added to "deletes" node in network
2. deleteRecipes_fail_confirmCorrectDeletesMade()
    - This is a complex one:
        - The use-case will attempt to delete all recipes passed as input. If there
        is an error with a particular delete, it continues with the others. But the
        resulting msg is DELETE_RECIPES_ERRORS. So we need to do rigorous checks here
        to make sure the correct recipes were deleted and the correct recipes were not.
    a) select a handful of random recipes for deleting
    b) change the ids of a few recipes so they will cause errors when deleting
    c) confirm DELETE_RECIPES_ERRORS msg is emitted from flow
    d) confirm ONLY the valid recipes are deleted from network "recipes" node
    e) confirm ONLY the valid recipes are inserted into network "deletes" node
    f) confirm ONLY the valid recipes are deleted from cache
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) select a handful of random recipes for deleting
    b) force an exception to be thrown on one of them
    c) confirm DELETE_RECIPES_ERRORS msg is emitted from flow
    d) confirm ONLY the valid recipes are deleted from network "recipes" node
    e) confirm ONLY the valid recipes are inserted into network "deletes" node
    f) confirm ONLY the valid recipes are deleted from cache
 */
@InternalCoroutinesApi
class DeleteMultipleRecipesTest {


    // system in test
    private var deleteMultipleRecipes: DeleteMultipleRecipes? = null

    // dependencies
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var recipeCacheDataSource: RecipeCacheDataSource
    private lateinit var recipeNetworkDataSource: RecipeNetworkDataSource
    private lateinit var recipeFactory: RecipeFactory

    @AfterEach
    fun afterEach() {
        deleteMultipleRecipes = null
    }

    @BeforeEach
    fun beforeEach() {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        recipeCacheDataSource = dependencyContainer.recipeCacheDataSource
        recipeNetworkDataSource = dependencyContainer.recipeNetworkDataSource
        recipeFactory = dependencyContainer.recipeFactory
        deleteMultipleRecipes = DeleteMultipleRecipes(
            recipeCacheDataSource = recipeCacheDataSource,
            recipeNetworkDataSource = recipeNetworkDataSource
        )
    }

    @Test
    fun deleteRecipes_success_confirmNetworkAndCacheUpdated() = runBlocking {

        val randomRecipes: ArrayList<Recipe> = ArrayList()
        val recipesInCache = recipeCacheDataSource.searchRecipes("", "", 1)

        for (recipe in recipesInCache) {
            randomRecipes.add(recipe)
            if (randomRecipes.size > 4) {
                break
            }
        }

        deleteMultipleRecipes?.deleteRecipes(
            recipes = randomRecipes,
            stateEvent = DeleteMultipleRecipesEvent(randomRecipes)
        )?.collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {

                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_RECIPES_SUCCESS
                )
            }
        })


        // confirm recipes are delted from cache
        for (recipe in randomRecipes) {
            val recipeInCache = recipeCacheDataSource.searchRecipeById(recipe.id)
            assertTrue { recipeInCache == null }
        }

        // confirm recipes are deleted from "recipes" node in network
        val doRecipesExistInNetwork = recipeNetworkDataSource.getAllRecipes()
            .containsAll(randomRecipes)
        assertFalse { doRecipesExistInNetwork }


        // confirm recipes are added to "deletes" node in network
        val deletedNetworkRecipes = recipeNetworkDataSource.getDeletedRecipes()
        assertTrue { deletedNetworkRecipes.containsAll(randomRecipes) }

    }


    @Test
    fun deleteRecipes_fail_confirmCorrectDeletesMade() = runBlocking {

        val validRecipes: ArrayList<Recipe> = ArrayList()
        val invalidRecipes: ArrayList<Recipe> = ArrayList()
        val recipesInCache = recipeCacheDataSource.searchRecipes("", "", 1)
        for (index in 0..recipesInCache.size) {
            var recipe: Recipe
            if (index % 2 == 0) {
                recipe = recipeFactory.createSingleRecipe(
                    id = UUID.randomUUID().toString(),
                    title = recipesInCache.get(index).title,
                    ingredients = recipesInCache.get(index).ingredients,
                    steps = recipesInCache.get(index).steps,
                    imageUrl = recipesInCache.get(index).imageUrl
                )
                invalidRecipes.add(recipe)
            } else {
                recipe = recipesInCache.get(index)
                validRecipes.add(recipe)
            }
            if ((invalidRecipes.size + validRecipes.size) > 4) {
                break
            }
        }

        val recipesToDelete = ArrayList(validRecipes + invalidRecipes)
        deleteMultipleRecipes?.deleteRecipes(
            recipes = recipesToDelete,
            stateEvent = DeleteMultipleRecipesEvent(recipesToDelete)
        )?.collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {

                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_RECIPES_ERRORS
                )
            }
        })


        // confirm ONLY the valid recipes are deleted from network "recipes" node
        val networkRecipes = recipeNetworkDataSource.getAllRecipes()
        assertFalse { networkRecipes.containsAll(validRecipes) }

        // confirm ONLY the valid recipes are inserted into network "deletes" node
        val deletedNetworkRecipes = recipeNetworkDataSource.getDeletedRecipes()
        assertTrue { deletedNetworkRecipes.containsAll(validRecipes) }
        assertFalse { deletedNetworkRecipes.containsAll(invalidRecipes) }


        // confirm ONLY the valid recipes are deleted from cache
        for (recipe in validRecipes) {
            val recipeInCache = recipeCacheDataSource.searchRecipeById(recipe.id)
            assertTrue { recipeInCache == null }
        }
        val numRecipesInCache = recipeCacheDataSource.getNumRecipes()
        assertTrue { numRecipesInCache == (recipesInCache.size - validRecipes.size) }

    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {

        val validRecipes: ArrayList<Recipe> = ArrayList()
        val invalidRecipes: ArrayList<Recipe> = ArrayList()
        val recipesInCache = recipeCacheDataSource.searchRecipes("", "", 1)
        for (recipe in recipesInCache) {
            validRecipes.add(recipe)
            if (validRecipes.size > 4) {
                break
            }
        }

        val errorRecipe = Recipe(
            id = FORCE_DELETE_RECIPE_EXCEPTION,
            title = UUID.randomUUID().toString(),
            ingredients = UUID.randomUUID().toString(),
            steps = UUID.randomUUID().toString(),
            imageUrl = UUID.randomUUID().toString(),
            updated_at = UUID.randomUUID().toString(),
            created_at = UUID.randomUUID().toString()
        )
        invalidRecipes.add(errorRecipe)

        val recipesToDelete = ArrayList(validRecipes + invalidRecipes)
        deleteMultipleRecipes?.deleteRecipes(
            recipes = recipesToDelete,
            stateEvent = DeleteMultipleRecipesEvent(recipesToDelete)
        )?.collect(object : FlowCollector<DataState<RecipeListViewState>?> {
            override suspend fun emit(value: DataState<RecipeListViewState>?) {

                assertEquals(
                    value?.stateMessage?.response?.message,
                    DELETE_RECIPES_ERRORS
                )
            }
        })


        // confirm ONLY the valid recipes are deleted from network "recipes" node
        val networkRecipes = recipeNetworkDataSource.getAllRecipes()
        assertFalse { networkRecipes.containsAll(validRecipes) }

        // confirm ONLY the valid recipes are inserted into network "deletes" node
        val deletedNetworkRecipes = recipeNetworkDataSource.getDeletedRecipes()
        assertTrue { deletedNetworkRecipes.containsAll(validRecipes) }
        assertFalse { deletedNetworkRecipes.containsAll(invalidRecipes) }


        // confirm ONLY the valid recipes are deleted from cache
        for (recipe in validRecipes) {
            val recipeInCache = recipeCacheDataSource.searchRecipeById(recipe.id)
            assertTrue { recipeInCache == null }
        }
        val numRecipesInCache = recipeCacheDataSource.getNumRecipes()
        assertTrue { numRecipesInCache == (recipesInCache.size - validRecipes.size) }
    }

}