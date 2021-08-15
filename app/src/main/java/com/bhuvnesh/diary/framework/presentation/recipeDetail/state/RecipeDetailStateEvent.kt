package com.bhuvnesh.diary.framework.presentation.recipeDetail.state

import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.StateEvent
import com.bhuvnesh.diary.business.domain.state.StateMessage


sealed class RecipeDetailStateEvent : StateEvent {


    object UpdateRecipeEvent : RecipeDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error updating recipe."
        }

        override fun eventName(): String {
            return "UpdateRecipeEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class DeleteRecipeEvent(
        val recipe: Recipe
    ) : RecipeDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error deleting recipe."
        }

        override fun eventName(): String {
            return "DeleteRecipeEvent"
        }

        override fun shouldDisplayProgressBar() = true
    }

    class CreateStateMessageEvent(
        val stateMessage: StateMessage
    ) : RecipeDetailStateEvent() {

        override fun errorInfo(): String {
            return "Error creating a new state message."
        }

        override fun eventName(): String {
            return "CreateStateMessageEvent"
        }

        override fun shouldDisplayProgressBar() = false
    }

}
