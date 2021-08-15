package com.bhuvnesh.diary.framework.dataSource.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bhuvnesh.diary.framework.dataSource.cache.model.RecipeCacheEntity

@Database(entities = [RecipeCacheEntity::class], version = 1, exportSchema = false)
abstract class RecipeDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        val DATABASE_NAME: String = "recipe_db"
    }


}