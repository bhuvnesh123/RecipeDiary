package com.bhuvnesh.diary.framework.presentation.recipeDetail.state

import android.os.Parcelable
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecipeDetailViewState(

    var recipe: Recipe? = null,

    var isUpdatePending: Boolean? = null

) : Parcelable, ViewState