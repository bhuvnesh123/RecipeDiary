package com.bhuvnesh.diary.framework.presentation.recipeNew.state

import android.os.Parcelable
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecipeInsertNewViewState(var newRecipe: Recipe? = null) : Parcelable,
    ViewState