package com.bhuvnesh.diary.business.interactors.recipelist

import com.bhuvnesh.diary.business.interactors.common.DeleteMultipleRecipes
import com.bhuvnesh.diary.framework.presentation.recipeList.state.RecipeListViewState


// Use cases
class RecipeListInteractors(
    val deleteRecipe: DeleteRecipe<RecipeListViewState>,
    val searchRecipes: SearchRecipes,
    val getNumRecipes: GetNumRecipes,
    val deleteMultipleRecipes: DeleteMultipleRecipes
)