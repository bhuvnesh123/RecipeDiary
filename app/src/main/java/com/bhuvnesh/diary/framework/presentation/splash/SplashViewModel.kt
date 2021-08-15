package com.bhuvnesh.diary.framework.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplashViewModel
@Inject
constructor(
    private val recipeNetworkSyncManager: RecipeNetworkSyncManager
) : ViewModel() {

    init {
        syncCacheWithNetwork()
    }

    fun hasSyncBeenExecuted() = recipeNetworkSyncManager.hasSyncBeenExecuted

    private fun syncCacheWithNetwork() {
        recipeNetworkSyncManager.executeDataSync(viewModelScope)
    }

}

