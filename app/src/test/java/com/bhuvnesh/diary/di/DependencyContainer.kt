package com.bhuvnesh.diary.di

import com.bhuvnesh.diary.business.data.RecipeDataFactory
import com.bhuvnesh.diary.business.data.cache.FakeRecipeCacheDataSourceImpl
import com.bhuvnesh.diary.business.data.cache.FakeRecipeNetworkDataSourceImpl
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DependencyContainer {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
    val dateUtil = DateUtil(dateFormat)
    lateinit var recipeNetworkDataSource: RecipeNetworkDataSource
    lateinit var recipeCacheDataSource: RecipeCacheDataSource
    lateinit var recipeFactory: RecipeFactory
    lateinit var recipeDataFactory: RecipeDataFactory


    init {
        isUnitTest = true // for Logger.kt
    }

    lateinit var recipesData: HashMap<String, Recipe>

    fun build() {
        this.javaClass.classLoader?.let { classLoader ->
            recipeDataFactory = RecipeDataFactory(classLoader)

            // fake data set
            recipesData = recipeDataFactory.produceHashMapOfRecipes(
                recipeDataFactory.produceListOfRecipes()
            )
        }
        recipeFactory = RecipeFactory(dateUtil)
        recipeNetworkDataSource = FakeRecipeNetworkDataSourceImpl(
            recipesData = recipesData,
            deletedRecipesData = HashMap()
        )
        recipeCacheDataSource = FakeRecipeCacheDataSourceImpl(
            recipesData = recipesData,
            dateUtil = dateUtil
        )
    }

}
