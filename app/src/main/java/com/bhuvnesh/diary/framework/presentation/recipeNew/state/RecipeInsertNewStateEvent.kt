package com.bhuvnesh.diary.framework.presentation.recipeNew.state

import com.bhuvnesh.diary.business.domain.state.StateEvent

sealed class RecipeInsertNewStateEvent : StateEvent {
    class InsertNewRecipeEvent(
        val newTitle: String,
        val newIngredients: String?,
        val newSteps: String,
        val newImageUrl: String?
    ) : RecipeInsertNewStateEvent() {
        override fun errorInfo(): String {
            return "Error inserting new recipe."
        }

        override fun eventName(): String {
            return "InsertNewRecipeEvent"
        }

        override fun shouldDisplayProgressBar(): Boolean = true

    }
}