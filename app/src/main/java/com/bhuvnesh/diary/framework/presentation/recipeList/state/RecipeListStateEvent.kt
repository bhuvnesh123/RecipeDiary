package com.bhuvnesh.diary.framework.presentation.recipeList.state

import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.StateEvent
import com.bhuvnesh.diary.business.domain.state.StateMessage

sealed class RecipeListStateEvent : StateEvent {

    class DeleteRecipeEvent(
        val recipe: Recipe
    ) : RecipeListStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting recipe."
        }

        override fun eventName(): String {
            return "DeleteRecipeEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean = true
    }

}

class DeleteMultipleRecipesEvent(
    val recipes: List<Recipe>
) : RecipeListStateEvent() {

    override fun errorInfo(): String {
        return "Error deleting the selected recipes."
    }

    override fun eventName(): String {
        return "DeleteMultipleRecipesEvent"
    }

    override fun shouldDisplayProgressBar() = true
}

class RestoreDeletedRecipeEvent(
    val recipe: Recipe
) : RecipeListStateEvent() {

    override fun errorInfo(): String {
        return "Error restoring the recipe that was deleted."
    }

    override fun eventName(): String {
        return "RestoreDeletedRecipeEvent"
    }

    override fun shouldDisplayProgressBar() = false
}

class SearchRecipesEvent(
    val clearLayoutManagerState: Boolean = true
) : RecipeListStateEvent() {

    override fun errorInfo(): String {
        return "Error getting list of recipes."
    }

    override fun eventName(): String {
        return "SearchRecipesEvent"
    }

    override fun shouldDisplayProgressBar() = true
}

object GetNumRecipesInCacheEvent : RecipeListStateEvent() {

    override fun errorInfo(): String {
        return "Error getting the number of recipes from the cache."
    }

    override fun eventName(): String {
        return "GetNumRecipesInCacheEvent"
    }

    override fun shouldDisplayProgressBar() = true
}

class CreateStateMessageEvent(
    val stateMessage: StateMessage
) : RecipeListStateEvent() {

    override fun errorInfo(): String {
        return "Error creating a new state message."
    }

    override fun eventName(): String {
        return "CreateStateMessageEvent"
    }

    override fun shouldDisplayProgressBar() = false
}
