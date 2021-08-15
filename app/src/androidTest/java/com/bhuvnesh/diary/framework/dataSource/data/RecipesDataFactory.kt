package com.bhuvnesh.diary.framework.dataSource.data

import android.app.Application
import android.content.res.AssetManager
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipesDataFactory
@Inject
constructor(
    private val application: Application,
    private val recipeFactory: RecipeFactory
) {

    fun produceListOfRecipes(): List<Recipe> {
        val recipes: List<Recipe> = Gson()
            .fromJson(
                getRecipesFromFile("recipe_list.json"),
                object : TypeToken<List<Recipe>>() {}.type
            )
        return recipes
    }

    fun produceEmptyListOfRecipes(): List<Recipe> {
        return ArrayList()
    }

    fun getRecipesFromFile(fileName: String): String? {
        return readJSONFromAsset(fileName)
    }

    private fun readJSONFromAsset(fileName: String): String? {
        var json: String? = null
        json = try {
            val inputStream: InputStream = (application.assets as AssetManager).open(fileName)
            inputStream.bufferedReader().use { it.readText() }
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun createSingleRecipe(
        id: String? = null,
        title: String,
        ingredients: String?,
        steps: String,
        imageUrl: String?
    ) = recipeFactory.createSingleRecipe(id, title, ingredients, steps, imageUrl)

    fun createRecipeList(numRecipes: Int) = recipeFactory.createRecipeList(numRecipes)
}
