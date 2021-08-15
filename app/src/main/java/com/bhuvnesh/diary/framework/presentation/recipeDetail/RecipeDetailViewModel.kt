package com.bhuvnesh.diary.framework.presentation.recipeDetail

import com.bhuvnesh.diary.business.domain.state.StateEvent
import com.bhuvnesh.diary.business.interactors.recipedetail.RecipeDetailInteractors
import com.bhuvnesh.diary.framework.presentation.common.BaseViewModel
import com.bhuvnesh.diary.framework.presentation.recipeDetail.state.RecipeDetailViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class RecipeDetailViewModel
@Inject
constructor(
    private val recipeDetailInteractors: RecipeDetailInteractors
) : BaseViewModel<RecipeDetailViewState>() {

    override fun handleNewData(data: RecipeDetailViewState) {

    }

    override fun setStateEvent(stateEvent: StateEvent) {

    }

    override fun initNewViewState(): RecipeDetailViewState {
        return RecipeDetailViewState()
    }

}



















