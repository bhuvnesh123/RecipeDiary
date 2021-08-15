package com.bhuvnesh.diary.framework.dataSource.cache.abstraction

import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.framework.dataSource.cache.database.RECIPE_PAGINATION_PAGE_SIZE

interface RecipeDaoService {
    suspend fun insertRecipe(recipe: Recipe): Long
    suspend fun insertRecipes(recipes: List<Recipe>): LongArray
    suspend fun deleteRecipe(id: String): Int
    suspend fun deleteRecipes(recipes: List<Recipe>): Int
    suspend fun updateRecipe(
        primaryKey: String,
        newTitle: String,
        newIngredients: String?,
        newSteps: String?,
        newImageUrl: String?,
        timeStamp: String?
    ): Int

    suspend fun searchRecipeById(id: String): Recipe?
    suspend fun getNumRecipes(): Int

    suspend fun searchRecipesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = RECIPE_PAGINATION_PAGE_SIZE
    ): List<Recipe>

    suspend fun searchRecipesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = RECIPE_PAGINATION_PAGE_SIZE
    ): List<Recipe>

    suspend fun searchRecipesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int = RECIPE_PAGINATION_PAGE_SIZE
    ): List<Recipe>

    suspend fun searchRecipesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int = RECIPE_PAGINATION_PAGE_SIZE
    ): List<Recipe>

    suspend fun getAllRecipes(): List<Recipe>

    suspend fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Recipe>
}