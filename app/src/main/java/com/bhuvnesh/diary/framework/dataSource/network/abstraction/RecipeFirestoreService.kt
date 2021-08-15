package com.bhuvnesh.diary.framework.dataSource.network.abstraction

import com.bhuvnesh.diary.business.domain.model.Recipe

interface RecipeFirestoreService {
    suspend fun insertOrUpdateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: String)
    suspend fun insertDeletedRecipe(recipe: Recipe)
    suspend fun deleteDeletedRecipe(recipe: Recipe)
    suspend fun getDeletedRecipes(): List<Recipe>
    suspend fun deleteAllRecipes()
    suspend fun searchRecipe(recipe: Recipe): Recipe?
    suspend fun getAllRecipes(): List<Recipe>
    suspend fun insertOrUpdateRecipes(recipes: List<Recipe>)
}