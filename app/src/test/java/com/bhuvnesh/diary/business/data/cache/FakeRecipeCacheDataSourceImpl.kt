package com.bhuvnesh.diary.business.data.cache

import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.framework.dataSource.cache.database.RECIPE_PAGINATION_PAGE_SIZE


const val FORCE_DELETE_RECIPE_EXCEPTION = "FORCE_DELETE_RECIPE_EXCEPTION"
const val FORCE_DELETES_RECIPE_EXCEPTION = "FORCE_DELETES_RECIPE_EXCEPTION"
const val FORCE_UPDATE_RECIPE_EXCEPTION = "FORCE_UPDATE_RECIPE_EXCEPTION"
const val FORCE_NEW_RECIPE_EXCEPTION = "FORCE_NEW_RECIPE_EXCEPTION"
const val FORCE_SEARCH_RECIPES_EXCEPTION = "FORCE_SEARCH_RECIPES_EXCEPTION"
const val FORCE_GENERAL_FAILURE = "FORCE_GENERAL_FAILURE"

class FakeRecipeCacheDataSourceImpl
constructor(private val recipesData: HashMap<String, Recipe>, private val dateUtil: DateUtil) :
    RecipeCacheDataSource {
    override suspend fun insertRecipe(recipe: Recipe): Long {
        if (recipe.id.equals(FORCE_NEW_RECIPE_EXCEPTION)) {
            throw Exception("Something went wrong inserting the RECIPE.")
        }
        if (recipe.id.equals(FORCE_GENERAL_FAILURE)) {
            return -1 // fail
        }
        recipesData.put(recipe.id, recipe)
        return 1 // success    }
    }

    override suspend fun deleteRecipe(primaryKey: String): Int {
        if (primaryKey.equals(FORCE_DELETE_RECIPE_EXCEPTION)) {
            throw Exception("Something went wrong deleting the RECIPE.")
        } else if (primaryKey.equals(FORCE_DELETES_RECIPE_EXCEPTION)) {
            throw Exception("Something went wrong deleting the RECIPE.")
        }
        return recipesData.remove(primaryKey)?.let {
            1 // return 1 for success
        } ?: -1 // -1 for failure    }
    }

    override suspend fun deleteRecipes(recipes: List<Recipe>): Int {
        var failOrSuccess = 1
        for (recipe in recipes) {
            if (recipesData.remove(recipe.id) == null) {
                failOrSuccess = -1 // mark for failure
            }
        }
        return failOrSuccess
    }


    override suspend fun updateRecipe(
        primaryKey: String,
        newTitle: String,
        newIngredients: String?,
        newSteps: String,
        newImageUrl: String?
    ): Int {
        if (primaryKey.equals(FORCE_UPDATE_RECIPE_EXCEPTION)) {
            throw Exception("Something went wrong updating the RECIPE.")
        }
        val updatedRECIPE = Recipe(
            id = primaryKey,
            title = newTitle,
            ingredients = newIngredients ?: "",
            steps = newSteps ?: "",
            imageUrl = newImageUrl ?: "",
            updated_at = dateUtil.getCurrentTimestamp(),
            created_at = recipesData.get(primaryKey)?.created_at ?: dateUtil.getCurrentTimestamp()
        )
        return recipesData.get(primaryKey)?.let {
            recipesData.put(primaryKey, updatedRECIPE)
            1 // success
        } ?: -1 // nothing to update
    }

    override suspend fun searchRecipes(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Recipe> {
        if (query.equals(FORCE_SEARCH_RECIPES_EXCEPTION)) {
            throw Exception("Something went searching the cache for RECIPEs.")
        }
        val results: ArrayList<Recipe> = ArrayList()
        for (RECIPE in recipesData.values) {
            if (RECIPE.title.contains(query)) {
                results.add(RECIPE)
            }
            if (results.size > (page * RECIPE_PAGINATION_PAGE_SIZE)) {
                break
            }
        }
        return results
    }

    override suspend fun searchRecipeById(id: String): Recipe? {
        return recipesData.get(id)
    }

    override suspend fun getNumRecipes(): Int {
        return recipesData.size
    }

    override suspend fun getAllRecipes(): List<Recipe> {
        return ArrayList(recipesData.values)
    }

    override suspend fun insertRecipes(recipes: List<Recipe>): LongArray {
        val results = LongArray(recipes.size)
        for ((index, recipe) in recipes.withIndex()) {
            results[index] = 1
            recipesData.put(recipe.id, recipe)
        }
        return results
    }

}