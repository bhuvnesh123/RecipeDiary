package com.bhuvnesh.diary.framework.dataSource.cache.implementation

import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.framework.dataSource.cache.abstraction.RecipeDaoService
import com.bhuvnesh.diary.framework.dataSource.cache.database.RecipeDao
import com.bhuvnesh.diary.framework.dataSource.cache.database.returnOrderedQuery
import com.bhuvnesh.diary.framework.dataSource.cache.mappers.CacheMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeDaoServiceImpl
@Inject
constructor(
    private val recipeDao: RecipeDao,
    private val recipeMapper: CacheMapper,
    private val dateUtil: DateUtil
) : RecipeDaoService {

    override suspend fun insertRecipe(recipe: Recipe): Long {
        return recipeDao.insertRecipe(recipeMapper.mapToEntity(recipe))
    }

    override suspend fun insertRecipes(recipes: List<Recipe>): LongArray {
        return recipeDao.insertRecipes(
            recipeMapper.recipeListToEntityList(recipes)
        )
    }

    override suspend fun searchRecipeById(id: String): Recipe? {
        return recipeDao.searchRecipeById(id)?.let { recipe ->
            recipeMapper.mapFromEntity(recipe)
        }
    }

    override suspend fun updateRecipe(
        primaryKey: String,
        newTitle: String,
        newIngredients: String?,
        newSteps: String?,
        newImageUrl: String?,
        timeStamp: String?
    ): Int {
        return recipeDao.updateRecipe(
            primaryKey = primaryKey,
            title = newTitle,
            ingredients = newIngredients,
            steps = newSteps,
            imageUrl = newImageUrl,
            updated_at = if (timeStamp == null) dateUtil.getCurrentTimestamp() else timeStamp
        )
    }

    override suspend fun deleteRecipe(primaryKey: String): Int {
        return recipeDao.deleteRecipe(primaryKey)
    }

    override suspend fun deleteRecipes(recipes: List<Recipe>): Int {
        val ids = recipes.mapIndexed { index, value -> value.id }
        return recipeDao.deleteRecipes(ids)
    }

    override suspend fun searchRecipesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Recipe> {
        return recipeMapper.entityListToRecipeList(
            recipeDao.searchRecipesOrderByDateDESC(
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchRecipesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Recipe> {
        return recipeMapper.entityListToRecipeList(
            recipeDao.searchRecipesOrderByDateASC(
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchRecipesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Recipe> {
        return recipeMapper.entityListToRecipeList(
            recipeDao.searchRecipesOrderByTitleDESC(
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun searchRecipesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Recipe> {
        return recipeMapper.entityListToRecipeList(
            recipeDao.searchRecipesOrderByTitleASC(
                query = query,
                page = page,
                pageSize = pageSize
            )
        )
    }

    override suspend fun getAllRecipes(): List<Recipe> {
        return recipeMapper.entityListToRecipeList(
            recipeDao.searchRecipes()
        )
    }

    override suspend fun getNumRecipes(): Int {
        return recipeDao.getNumRecipes()
    }

    override suspend fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Recipe> {
        return recipeMapper.entityListToRecipeList(
            recipeDao.returnOrderedQuery(
                query = query,
                page = page,
                filterAndOrder = filterAndOrder
            )
        )
    }
}

