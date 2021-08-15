package com.bhuvnesh.diary.business.data.cache.implementation

import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.framework.dataSource.cache.abstraction.RecipeDaoService
import javax.inject.Inject

class RecipeCacheDataSourceImpl @Inject constructor(private val recipeDaoService: RecipeDaoService) :
    RecipeCacheDataSource {
    override suspend fun insertRecipe(recipe: Recipe): Long {
        return recipeDaoService.insertRecipe(recipe)
    }

    override suspend fun deleteRecipe(id: String): Int {
        return recipeDaoService.deleteRecipe(id)
    }

    override suspend fun deleteRecipes(recipes: List<Recipe>): Int {
        return recipeDaoService.deleteRecipes(recipes)
    }

    override suspend fun updateRecipe(
        primaryKey: String,
        newTitle: String,
        newIngredients: String?,
        newSteps: String,
        newImageUrl: String?,
        newUpdatedAt: String?
    ): Int {
        return recipeDaoService.updateRecipe(
            primaryKey,
            newTitle,
            newIngredients,
            newSteps,
            newImageUrl,
            newUpdatedAt
        )
    }

    override suspend fun searchRecipes(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Recipe> {
        return recipeDaoService.returnOrderedQuery(
            query, filterAndOrder, page
        )
    }

    override suspend fun searchRecipeById(id: String): Recipe? {
        return recipeDaoService.searchRecipeById(id)
    }

    override suspend fun getNumRecipes(): Int {
        return recipeDaoService.getNumRecipes()
    }

    override suspend fun getAllRecipes(): List<Recipe> {
        return recipeDaoService.getAllRecipes()
    }

    override suspend fun insertRecipes(recipes: List<Recipe>): LongArray {
        return recipeDaoService.insertRecipes(recipes)

    }
}

