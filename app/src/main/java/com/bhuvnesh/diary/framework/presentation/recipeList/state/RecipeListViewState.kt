package com.bhuvnesh.diary.framework.presentation.recipeList.state

import android.os.Parcelable
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecipeListViewState(
    var recipeList: ArrayList<Recipe>? = null,
    var newRecipe: Recipe? = null, // Recipe that can be created with fab
    var searchQuery: String? = null,
    var page: Int? = null,
    var isQueryExhausted: Boolean? = null,
    var filter: String? = null,
    var order: String? = null,
    var layoutManagerState: Parcelable? = null,
    var numRecipesInCache: Int? = null
) : Parcelable,
    ViewState {

    @Parcelize
    data class RecipePendingDelete(
        var recipe: Recipe? = null,
        var listPosition: Int? = null
    ) : Parcelable
}