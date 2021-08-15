package com.bhuvnesh.diary.business.interactors.recipedetail

import com.bhuvnesh.diary.business.interactors.recipelist.DeleteRecipe
import com.bhuvnesh.diary.framework.presentation.recipeDetail.state.RecipeDetailViewState


// Use cases
class RecipeDetailInteractors(
    val deleteRecipe: DeleteRecipe<RecipeDetailViewState>,
    val updateRecipe: UpdateRecipe
)