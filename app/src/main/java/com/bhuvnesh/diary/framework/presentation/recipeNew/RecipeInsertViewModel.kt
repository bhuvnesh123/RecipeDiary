package com.bhuvnesh.diary.framework.presentation.recipeNew

import androidx.compose.runtime.mutableStateOf
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.DataState
import com.bhuvnesh.diary.business.domain.state.StateEvent
import com.bhuvnesh.diary.business.interactors.recipeInsertNew.RecipeInsertNewInteractors
import com.bhuvnesh.diary.framework.presentation.common.BaseViewModel
import com.bhuvnesh.diary.framework.presentation.recipeNew.state.RecipeInsertNewStateEvent
import com.bhuvnesh.diary.framework.presentation.recipeNew.state.RecipeInsertNewViewState
import kotlinx.coroutines.flow.Flow
import java.util.*

class RecipeInsertViewModel constructor(private val recipeInsertNewInteractors: RecipeInsertNewInteractors) :
    BaseViewModel<RecipeInsertNewViewState>() {
    val title = mutableStateOf("")
    val ingredients = mutableStateOf("")
    val textFieldCount = mutableStateOf(3)
    val stepsList = TreeMap<Int, String>()
    val snackbarVisibleState = mutableStateOf(false)
    val snackbarText = mutableStateOf("")


    fun onTitleChanged(title: String) {
        this.title.value = title
    }

    fun onIngredientsChanged(ingredients: String) {
        this.ingredients.value = ingredients
    }

    fun setSteps(stepNo: Int, stepData: String) {
        stepsList[stepNo] = stepData
    }

    override fun handleNewData(data: RecipeInsertNewViewState) {
        data.let { viewState ->

            viewState.newRecipe?.let { recipe ->
                setRecipe(recipe)
            }

        }
    }

    private fun setRecipe(recipe: Recipe?) {
        val update = getCurrentViewStateOrNew()
        update.newRecipe = recipe
        setViewState(update)
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        val job: Flow<DataState<RecipeInsertNewViewState>?> = when (stateEvent) {

            is RecipeInsertNewStateEvent.InsertNewRecipeEvent -> {
                recipeInsertNewInteractors.insertNewRecipe.insertNewRecipe(
                    id = null,
                    title = stateEvent.newTitle,
                    ingredients = stateEvent.newIngredients,
                    steps = stateEvent.newSteps,
                    imageUrl = stateEvent.newImageUrl,
                    stateEvent = stateEvent
                )
            }
            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }
        launchJob(stateEvent, job)
    }

    override fun initNewViewState(): RecipeInsertNewViewState {
        return RecipeInsertNewViewState()
    }

    fun convertStepListToString(): String {
        val step = StringBuilder()
        stepsList.entries.forEachIndexed { index, entry ->
            step.append(entry.key)
            step.append(" . ")
            step.append(entry.value)
            if (index != stepsList.entries.size - 1) {
                step.append("\n")
            }
        }
        return step.toString()
    }

}