package com.bhuvnesh.diary.framework.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bhuvnesh.diary.business.interactors.splash.SyncDeletedRecipes
import com.bhuvnesh.diary.business.interactors.splash.SyncRecipes
import com.bhuvnesh.diary.util.printLogD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeNetworkSyncManager
@Inject
constructor(
    private val syncRecipes: SyncRecipes,
    private val syncDeletedRecipes: SyncDeletedRecipes
) {

    private val _hasSyncBeenExecuted: MutableLiveData<Boolean> = MutableLiveData(false)

    val hasSyncBeenExecuted: LiveData<Boolean>
        get() = _hasSyncBeenExecuted

    fun executeDataSync(coroutineScope: CoroutineScope) {
        if (_hasSyncBeenExecuted.value!!) {
            return
        }

        val syncJob = coroutineScope.launch {
            val deletesJob = launch {
                printLogD(
                    "SyncRecipes",
                    "syncing deleted Recipes."
                )
                syncDeletedRecipes.syncDeletedRecipes()
            }
            deletesJob.join()

            launch {
                printLogD(
                    "SyncRecipes",
                    "syncing Recipes."
                )
                syncRecipes.syncRecipes()
            }
        }
        syncJob.invokeOnCompletion {
            CoroutineScope(Main).launch {
                _hasSyncBeenExecuted.value = true
            }
        }
    }

}





















