package com.bhuvnesh.diary.business.data.cache.abstraction

import com.bhuvnesh.diary.business.domain.model.Recipe

interface RecipeCacheDataSource {
    suspend fun insertRecipe(recipe: Recipe): Long

    suspend fun deleteRecipe(id: String): Int

    suspend fun deleteRecipes(recipes: List<Recipe>): Int

    suspend fun updateRecipe(
        primaryKey: String,
        newTitle: String,
        newIngredients: String?,
        newSteps: String,
        newImageUrl: String?,
        newUpdatedAt: String?
    ): Int

    suspend fun searchRecipes(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Recipe>

    suspend fun searchRecipeById(id: String): Recipe?

    suspend fun getNumRecipes(): Int

    suspend fun getAllRecipes(): List<Recipe>

    suspend fun insertRecipes(recipes: List<Recipe>): LongArray

}