package com.bhuvnesh.diary.framework.presentation.recipeList

import android.content.SharedPreferences
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.state.DataState
import com.bhuvnesh.diary.business.domain.state.StateEvent
import com.bhuvnesh.diary.business.interactors.recipelist.RecipeListInteractors
import com.bhuvnesh.diary.framework.dataSource.cache.database.RECIPE_FILTER_DATE_CREATED
import com.bhuvnesh.diary.framework.dataSource.cache.database.RECIPE_FILTER_TITLE
import com.bhuvnesh.diary.framework.dataSource.cache.database.RECIPE_ORDER_ASC
import com.bhuvnesh.diary.framework.dataSource.cache.database.RECIPE_ORDER_DESC
import com.bhuvnesh.diary.framework.dataSource.preferences.PreferenceKeys.Companion.RECIPE_FILTER
import com.bhuvnesh.diary.framework.dataSource.preferences.PreferenceKeys.Companion.RECIPE_ORDER
import com.bhuvnesh.diary.framework.presentation.common.BaseViewModel
import com.bhuvnesh.diary.framework.presentation.recipeList.state.*
import com.bhuvnesh.diary.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

const val DELETE_PENDING_ERROR = "There is already a pending delete operation."

@ExperimentalCoroutinesApi
@FlowPreview
class RecipeListViewModel
constructor(
    private val recipeListInteractors: RecipeListInteractors,
    private val recipeFactory: RecipeFactory,
    private val editor: SharedPreferences.Editor,
    sharedPreferences: SharedPreferences
) : BaseViewModel<RecipeListViewState>() {

    val recipes: MutableState<List<Recipe>> = mutableStateOf(ArrayList())
    val query = mutableStateOf("")
    val shouldDialogOpen = mutableStateOf(false)
    val selectedFilter = mutableStateOf("")
    val selectedSort = mutableStateOf("")
    val snackbarVisibleState = mutableStateOf(false)
    val snackbarText = mutableStateOf("")


    init {
        setRecipeFilter(
            sharedPreferences.getString(
                RECIPE_FILTER,
                RECIPE_FILTER_DATE_CREATED
            )
        )
        setRecipeOrder(
            sharedPreferences.getString(
                RECIPE_ORDER,
                RECIPE_ORDER_DESC
            )
        )
    }

    fun onQueryChanged(query: String) {
        this.query.value = query
    }

    override fun handleNewData(data: RecipeListViewState) {

        data.let { viewState ->
            viewState.recipeList?.let { recipeList ->
                Log.e("BHUVNESH", recipeList.toString())
                setRecipeListData(recipeList)
            }

            viewState.numRecipesInCache?.let { numRecipes ->
                setNumRecipesInCache(numRecipes)
            }

            viewState.newRecipe?.let { recipe ->
                setRecipe(recipe)
            }
        }

    }

    override fun setStateEvent(stateEvent: StateEvent) {

        val job: Flow<DataState<RecipeListViewState>?> = when (stateEvent) {

            is RecipeListStateEvent.DeleteRecipeEvent -> {
                recipeListInteractors.deleteRecipe.deleteRecipe(
                    recipe = stateEvent.recipe,
                    stateEvent = stateEvent
                )
            }

            is DeleteMultipleRecipesEvent -> {
                recipeListInteractors.deleteMultipleRecipes.deleteRecipes(
                    recipes = stateEvent.recipes,
                    stateEvent = stateEvent
                )
            }


            is SearchRecipesEvent -> {
                if (stateEvent.clearLayoutManagerState) {
                    clearLayoutManagerState()
                }
                recipeListInteractors.searchRecipes.searchRecipes(
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage(),
                    stateEvent = stateEvent
                )
            }

            is GetNumRecipesInCacheEvent -> {
                recipeListInteractors.getNumRecipes.getNumRecipes(
                    stateEvent = stateEvent
                )
            }

            is CreateStateMessageEvent -> {
                emitStateMessageEvent(
                    stateMessage = stateEvent.stateMessage,
                    stateEvent = stateEvent
                )
            }

            else -> {
                emitInvalidStateEvent(stateEvent)
            }
        }
        launchJob(stateEvent, job)
    }

    /*
        Getters
     */
    private fun getFilter(): String {
        return getCurrentViewStateOrNew().filter
            ?: RECIPE_FILTER_DATE_CREATED
    }

    private fun getOrder(): String {
        return getCurrentViewStateOrNew().order
            ?: RECIPE_ORDER_DESC
    }

    private fun getSearchQuery(): String {
        return getCurrentViewStateOrNew().searchQuery
            ?: return ""
    }

    private fun getPage(): Int {
        return getCurrentViewStateOrNew().page
            ?: return 1
    }

    private fun getRecipeListSize() = getCurrentViewStateOrNew().recipeList?.size ?: 0

    private fun getNumRecipesInCache() = getCurrentViewStateOrNew().numRecipesInCache ?: 0

    // for debugging
    fun getActiveJobs() = dataChannelManager.getActiveJobs()

    fun getLayoutManagerState(): Parcelable? {
        return getCurrentViewStateOrNew().layoutManagerState
    }

    private fun findListPositionOfRecipe(recipe: Recipe?): Int {
        val viewState = getCurrentViewStateOrNew()
        viewState.recipeList?.let { recipeList ->
            for ((index, item) in recipeList.withIndex()) {
                if (item.id == recipe?.id) {
                    return index
                }
            }
        }
        return 0
    }

    fun isPaginationExhausted() = getRecipeListSize() >= getNumRecipesInCache()

    fun isQueryExhausted(): Boolean {
        printLogD(
            "RecipeListViewModel",
            "is query exhasuted? ${getCurrentViewStateOrNew().isQueryExhausted ?: true}"
        )
        return getCurrentViewStateOrNew().isQueryExhausted ?: true
    }

    override fun initNewViewState(): RecipeListViewState {
        return RecipeListViewState()
    }

    /*
        Setters
     */
    private fun setRecipeListData(recipesList: ArrayList<Recipe>) {
        val update = getCurrentViewStateOrNew()
        update.recipeList = recipesList
        setViewState(update)
    }

    fun setQueryExhausted(isExhausted: Boolean) {
        val update = getCurrentViewStateOrNew()
        update.isQueryExhausted = isExhausted
        setViewState(update)
    }

    // can be selected from Recyclerview or created new from dialog
    private fun setRecipe(recipe: Recipe?) {
        val update = getCurrentViewStateOrNew()
        update.newRecipe = recipe
        setViewState(update)
    }

    fun setQuery(query: String?) {
        val update = getCurrentViewStateOrNew()
        update.searchQuery = query
        setViewState(update)
    }


    // if a recipe is deleted and then restored, the id will be incorrect.
    // So need to reset it here.
    private fun setRestoredRecipeId(restoredRecipe: Recipe) {
        val update = getCurrentViewStateOrNew()
        update.recipeList?.let { recipeList ->
            for ((index, recipe) in recipeList.withIndex()) {
                if (recipe.title == restoredRecipe.title) {
                    recipeList.remove(recipe)
                    recipeList.add(index, restoredRecipe)
                    update.recipeList = recipeList
                    break
                }
            }
        }
        setViewState(update)
    }

    private fun removePendingRecipeFromList(recipe: Recipe?) {
        val update = getCurrentViewStateOrNew()
        val list = update.recipeList
        if (list?.contains(recipe) == true) {
            list.remove(recipe)
            update.recipeList = list
            setViewState(update)
        }
    }


    private fun setNumRecipesInCache(numRecipes: Int) {
        val update = getCurrentViewStateOrNew()
        update.numRecipesInCache = numRecipes
        setViewState(update)
    }

    private fun resetPage() {
        val update = getCurrentViewStateOrNew()
        update.page = 1
        setViewState(update)
    }

    fun clearList() {
        printLogD("ListViewModel", "clearList")
        val update = getCurrentViewStateOrNew()
        update.recipeList = ArrayList()
        setViewState(update)
    }

    private fun incrementPageNumber() {
        val update = getCurrentViewStateOrNew()
        val page = update.copy().page ?: 1
        update.page = page.plus(1)
        setViewState(update)
    }

    fun setLayoutManagerState(layoutManagerState: Parcelable) {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = layoutManagerState
        setViewState(update)
    }

    private fun clearLayoutManagerState() {
        val update = getCurrentViewStateOrNew()
        update.layoutManagerState = null
        setViewState(update)
    }

    private fun setRecipeFilter(filter: String?) {
        filter?.let {
            val update = getCurrentViewStateOrNew()
            update.filter = filter
            setViewState(update)

            selectedFilter.value = when (it) {
                RECIPE_FILTER_TITLE -> "Title"
                else -> "Date"
            }
        }
    }

    private fun setRecipeOrder(order: String?) {
        val update = getCurrentViewStateOrNew()
        update.order = order
        setViewState(update)
        selectedSort.value = when (order) {
            RECIPE_ORDER_ASC -> "Ascending"
            else -> "Descending"
        }
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(RECIPE_FILTER, filter)
        editor.apply()

        editor.putString(RECIPE_ORDER, order)
        editor.apply()
    }


    fun beginPendingDelete(recipe: Recipe) {
        removePendingRecipeFromList(recipe)
        setStateEvent(
            RecipeListStateEvent.DeleteRecipeEvent(
                recipe = recipe
            )
        )
    }

    fun loadFirstPage() {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(SearchRecipesEvent())
        printLogD(
            "RecipeListViewModel",
            "loadFirstPage: ${getCurrentViewStateOrNew().searchQuery}"
        )
    }

    fun nextPage() {
        if (!isQueryExhausted()) {
            printLogD("RecipeListViewModel", "attempting to load next page...")
            clearLayoutManagerState()
            incrementPageNumber()
            setStateEvent(SearchRecipesEvent())
        }
    }

    fun retrieveNumRecipesInCache() {
        setStateEvent(GetNumRecipesInCacheEvent)
    }

    fun refreshSearchQuery() {
        setQueryExhausted(false)
        setStateEvent(SearchRecipesEvent(false))
    }

    fun onFilterSelected(text: String) {
        when (text) {
            "Title" -> {
                editor.putString(RECIPE_FILTER, RECIPE_FILTER_TITLE)
                editor.apply()
                setRecipeFilter(RECIPE_FILTER_TITLE)

            }
            "Date" -> {
                editor.putString(RECIPE_FILTER, RECIPE_FILTER_DATE_CREATED)
                editor.apply()
                setRecipeFilter(RECIPE_FILTER_DATE_CREATED)
            }
        }
        clearList()
        loadFirstPage()
    }

    fun onSortSelected(text: String) {
        when (text) {
            "Ascending" -> {
                editor.putString(RECIPE_ORDER, RECIPE_ORDER_ASC)
                editor.apply()
                setRecipeOrder(RECIPE_ORDER_ASC)

            }
            "Descending" -> {
                editor.putString(RECIPE_ORDER, RECIPE_ORDER_DESC)
                editor.apply()
                setRecipeOrder(RECIPE_ORDER_DESC)
            }
        }
        clearList()
        loadFirstPage()
    }


}













