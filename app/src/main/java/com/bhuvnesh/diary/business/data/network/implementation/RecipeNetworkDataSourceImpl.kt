package com.bhuvnesh.diary.business.data.network.implementation

import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.framework.dataSource.network.abstraction.RecipeFirestoreService
import javax.inject.Inject

class RecipeNetworkDataSourceImpl @Inject constructor(private val recipeFirestoreService: RecipeFirestoreService) :
    RecipeNetworkDataSource {
    override suspend fun insertOrUpdateRecipe(recipe: Recipe) {
        recipeFirestoreService.insertOrUpdateRecipe(recipe)
    }

    override suspend fun deleteRecipe(id: String) {
        recipeFirestoreService.deleteRecipe(id)
    }

    override suspend fun insertDeletedRecipe(recipe: Recipe) {
        recipeFirestoreService.insertDeletedRecipe(recipe)
    }

    override suspend fun deleteDeletedRecipe(recipe: Recipe) {
        recipeFirestoreService.deleteDeletedRecipe(recipe)
    }

    override suspend fun insertOrUpdateRecipes(recipes: List<Recipe>) {
        recipeFirestoreService.insertOrUpdateRecipes(recipes)
    }

    override suspend fun getDeletedRecipes(): List<Recipe> {
        return recipeFirestoreService.getDeletedRecipes()
    }

    override suspend fun deleteAllRecipes() {
        return recipeFirestoreService.deleteAllRecipes()
    }

    override suspend fun searchRecipe(recipe: Recipe): Recipe? {
        return recipeFirestoreService.searchRecipe(recipe)
    }

    override suspend fun getAllRecipes(): List<Recipe> {
        return recipeFirestoreService.getAllRecipes()
    }

}