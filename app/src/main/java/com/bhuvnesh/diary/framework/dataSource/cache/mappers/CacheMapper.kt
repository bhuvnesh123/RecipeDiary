package com.bhuvnesh.diary.framework.dataSource.cache.mappers

import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.business.domain.utils.EntityMapper
import com.bhuvnesh.diary.framework.dataSource.cache.model.RecipeCacheEntity
import javax.inject.Inject

/**
 * Maps Recipe to RecipeCacheEntity or RecipeCacheEntity to Recipe.
 */
class CacheMapper
@Inject
constructor(
    private val dateUtil: DateUtil
) : EntityMapper<RecipeCacheEntity, Recipe> {

    fun entityListToRecipeList(entities: List<RecipeCacheEntity>): List<Recipe> {
        val list: ArrayList<Recipe> = ArrayList()
        for (entity in entities) {
            list.add(mapFromEntity(entity))
        }
        return list
    }

    fun recipeListToEntityList(recipes: List<Recipe>): List<RecipeCacheEntity> {
        val entities: ArrayList<RecipeCacheEntity> = ArrayList()
        for (recipe in recipes) {
            entities.add(mapToEntity(recipe))
        }
        return entities
    }

    override fun mapFromEntity(entity: RecipeCacheEntity): Recipe {
        return Recipe(
            id = entity.id,
            title = entity.title,
            ingredients = entity.ingredients,
            steps = entity.steps,
            imageUrl = entity.imageUrl,
            updated_at = entity.updated_at,
            created_at = entity.created_at
        )
    }

    override fun mapToEntity(domainModel: Recipe): RecipeCacheEntity {
        return RecipeCacheEntity(
            id = domainModel.id,
            title = domainModel.title,
            ingredients = domainModel.ingredients,
            steps = domainModel.steps,
            imageUrl = domainModel.imageUrl,
            updated_at = domainModel.updated_at,
            created_at = domainModel.created_at
        )
    }
}
