package com.bhuvnesh.diary.business.data

import com.bhuvnesh.diary.business.domain.model.Recipe
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class RecipeDataFactory(
    private val testClassLoader: ClassLoader
) {

    fun produceListOfRecipes(): List<Recipe> {
        val recipes: List<Recipe> = Gson()
            .fromJson(
                getRecipesFromFile("recipe_list.json"),
                object : TypeToken<List<Recipe>>() {}.type
            )
        return recipes
    }

    fun produceHashMapOfRecipes(recipeList: List<Recipe>): HashMap<String, Recipe> {
        val map = HashMap<String, Recipe>()
        for (recipe in recipeList) {
            map.put(recipe.id, recipe)
        }
        return map
    }

    fun produceEmptyListOfRecipes(): List<Recipe> {
        return ArrayList()
    }

    fun getRecipesFromFile(fileName: String): String {
        return testClassLoader.getResource(fileName).readText()
    }

}
