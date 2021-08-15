package com.bhuvnesh.diary.framework.dataSource.cache.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bhuvnesh.diary.framework.dataSource.cache.model.RecipeCacheEntity

const val RECIPE_ORDER_ASC: String = ""
const val RECIPE_ORDER_DESC: String = "-"
const val RECIPE_FILTER_TITLE = "title"
const val RECIPE_FILTER_DATE_CREATED = "created_at"

const val ORDER_BY_ASC_DATE_UPDATED = RECIPE_ORDER_ASC + RECIPE_FILTER_DATE_CREATED
const val ORDER_BY_DESC_DATE_UPDATED = RECIPE_ORDER_DESC + RECIPE_FILTER_DATE_CREATED
const val ORDER_BY_ASC_TITLE = RECIPE_ORDER_ASC + RECIPE_FILTER_TITLE
const val ORDER_BY_DESC_TITLE = RECIPE_ORDER_DESC + RECIPE_FILTER_TITLE

const val RECIPE_PAGINATION_PAGE_SIZE = 30


@Dao
interface RecipeDao {

    @Insert
    suspend fun insertRecipe(recipe: RecipeCacheEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipes(recipes: List<RecipeCacheEntity>): LongArray

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun searchRecipeById(id: String): RecipeCacheEntity?

    @Query("DELETE FROM recipes WHERE id IN (:ids)")
    suspend fun deleteRecipes(ids: List<String>): Int

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()

    @Query(
        """
        UPDATE recipes 
        SET 
        title = :title, 
        ingredients = :ingredients,
        steps = :steps,
        imageUrl = :imageUrl,
        updated_at = :updated_at
        WHERE id = :primaryKey
        """
    )
    suspend fun updateRecipe(
        primaryKey: String,
        title: String,
        ingredients: String?,
        steps: String?,
        imageUrl: String?,
        updated_at: String
    ): Int


    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeCacheEntity>

    @Query("DELETE FROM recipes WHERE id = :primaryKey")
    suspend fun deleteRecipe(primaryKey: String): Int

    @Query("SELECT * FROM recipes")
    suspend fun searchRecipes(): List<RecipeCacheEntity>

    @Query(
        """
        SELECT * FROM recipes 
        WHERE title LIKE '%' || :query || '%' 
        ORDER BY updated_at DESC LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchRecipesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = RECIPE_PAGINATION_PAGE_SIZE
    ): List<RecipeCacheEntity>

    @Query(
        """
        SELECT * FROM recipes 
        WHERE title LIKE '%' || :query || '%' 
        ORDER BY updated_at ASC LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchRecipesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = RECIPE_PAGINATION_PAGE_SIZE
    ): List<RecipeCacheEntity>

    @Query(
        """
        SELECT * FROM recipes 
        WHERE title LIKE '%' || :query || '%' 
        ORDER BY LOWER(title) DESC LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchRecipesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int = RECIPE_PAGINATION_PAGE_SIZE
    ): List<RecipeCacheEntity>

    @Query(
        """
        SELECT * FROM recipes 
        WHERE title LIKE '%' || :query || '%' 
        ORDER BY LOWER(title) ASC LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchRecipesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int = RECIPE_PAGINATION_PAGE_SIZE
    ): List<RecipeCacheEntity>


    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getNumRecipes(): Int
}


suspend fun RecipeDao.returnOrderedQuery(
    query: String,
    filterAndOrder: String,
    page: Int
): List<RecipeCacheEntity> {

    when {

        filterAndOrder.contains(ORDER_BY_DESC_DATE_UPDATED) -> {
            return searchRecipesOrderByDateDESC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_ASC_DATE_UPDATED) -> {
            return searchRecipesOrderByDateASC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_DESC_TITLE) -> {
            return searchRecipesOrderByTitleDESC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_ASC_TITLE) -> {
            return searchRecipesOrderByTitleASC(
                query = query,
                page = page
            )
        }
        else ->
            return searchRecipesOrderByDateDESC(
                query = query,
                page = page
            )
    }
}