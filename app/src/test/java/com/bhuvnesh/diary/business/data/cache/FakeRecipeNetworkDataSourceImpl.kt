package com.bhuvnesh.diary.business.data.cache

import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe

class FakeRecipeNetworkDataSourceImpl constructor(
    private val recipesData: HashMap<String, Recipe>,
    private val deletedRecipesData: HashMap<String, Recipe>
) : RecipeNetworkDataSource {
    override suspend fun insertOrUpdateRecipe(recipe: Recipe) {
        recipesData.put(recipe.id, recipe)
    }

    override suspend fun deleteRecipe(id: String) {
        recipesData.remove(id)
    }

    override suspend fun insertDeletedRecipe(recipe: Recipe) {
        deletedRecipesData.put(recipe.id, recipe)
    }

    override suspend fun deleteDeletedRecipe(recipe: Recipe) {
        deletedRecipesData.remove(recipe.id)
    }

    override suspend fun insertOrUpdateRecipes(recipes: List<Recipe>) {
        for (recipe in recipes) {
            recipesData.put(recipe.id, recipe)
        }
    }

    override suspend fun getDeletedRecipes(): List<Recipe> {
        return ArrayList(deletedRecipesData.values)
    }

    override suspend fun deleteAllRecipes() {
        return recipesData.clear()
    }

    override suspend fun searchRecipe(recipe: Recipe): Recipe? {
        return recipesData.get(recipe.id)
    }

    override suspend fun getAllRecipes(): List<Recipe> {
        return ArrayList(recipesData.values)
    }
}