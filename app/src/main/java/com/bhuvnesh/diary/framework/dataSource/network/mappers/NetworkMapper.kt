package com.bhuvnesh.diary.framework.dataSource.network.mappers

import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.business.domain.utils.EntityMapper
import com.bhuvnesh.diary.framework.dataSource.network.model.RecipeNetworkEntity
import javax.inject.Inject

/**
 * Maps Recipe to RecipeNetworkEntity or RecipeNetworkEntity to Recipe.
 */
class NetworkMapper
@Inject
constructor(
    private val dateUtil: DateUtil
) : EntityMapper<RecipeNetworkEntity, Recipe> {

    fun entityListToRecipeList(entities: List<RecipeNetworkEntity>): List<Recipe> {
        val list: ArrayList<Recipe> = ArrayList()
        for (entity in entities) {
            list.add(mapFromEntity(entity))
        }
        return list
    }

    fun recipeListToEntityList(recipes: List<Recipe>): List<RecipeNetworkEntity> {
        val entities: ArrayList<RecipeNetworkEntity> = ArrayList()
        for (recipe in recipes) {
            entities.add(mapToEntity(recipe))
        }
        return entities
    }

    override fun mapFromEntity(entity: RecipeNetworkEntity): Recipe {
        return Recipe(
            id = entity.id,
            title = entity.title,
            ingredients = entity.ingredients,
            steps = entity.steps,
            imageUrl = entity.imageUrl,
            updated_at = dateUtil.convertFirebaseTimestampToStringData(entity.updated_at),
            created_at = dateUtil.convertFirebaseTimestampToStringData(entity.created_at)
        )
    }

    override fun mapToEntity(domainModel: Recipe): RecipeNetworkEntity {
        return RecipeNetworkEntity(
            domainModel.id,
            domainModel.title,
            domainModel.ingredients,
            domainModel.steps,
            domainModel.imageUrl,
            updated_at = dateUtil.convertStringDateToFirebaseTimestamp(domainModel.updated_at),
            created_at = dateUtil.convertStringDateToFirebaseTimestamp(domainModel.created_at)
        )
    }


}

