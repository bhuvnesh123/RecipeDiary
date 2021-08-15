package com.bhuvnesh.diary.business.domain.model

import com.bhuvnesh.diary.business.domain.utils.DateUtil
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton

class RecipeFactory @Inject constructor(val dateUtil: DateUtil) {

    fun createSingleRecipe(
        id: String? = null,
        title: String,
        ingredients: String?,
        steps: String,
        imageUrl: String?
    ): Recipe {
        return Recipe(
            id ?: UUID.randomUUID().toString(),
            title,
            ingredients ?: "",
            steps,
            imageUrl ?: "",
            dateUtil.getCurrentTimestamp(),
            dateUtil.getCurrentTimestamp()
        )
    }

    fun createRecipeList(numRecipes: Int): List<Recipe> {
        val list: ArrayList<Recipe> = ArrayList()
        for (i in 0 until numRecipes) { // exclusive on upper bound
            list.add(
                createSingleRecipe(
                    id = UUID.randomUUID().toString(),
                    title = UUID.randomUUID().toString(),
                    ingredients = UUID.randomUUID().toString(),
                    steps = UUID.randomUUID().toString(),
                    imageUrl = UUID.randomUUID().toString()
                )
            )
        }
        return list
    }
}