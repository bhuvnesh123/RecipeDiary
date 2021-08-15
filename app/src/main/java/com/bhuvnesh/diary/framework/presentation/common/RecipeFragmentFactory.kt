package com.bhuvnesh.diary.framework.presentation.common

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.framework.presentation.recipeDetail.RecipeDetailFragment
import com.bhuvnesh.diary.framework.presentation.recipeList.RecipeListFragment
import com.bhuvnesh.diary.framework.presentation.recipeNew.RecipeInsertNewFragment
import com.bhuvnesh.diary.framework.presentation.splash.SplashFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class RecipeFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when (className) {

            RecipeListFragment::class.java.name -> {
                val fragment = RecipeListFragment(viewModelFactory, dateUtil)
                fragment
            }

            RecipeDetailFragment::class.java.name -> {
                val fragment = RecipeDetailFragment(viewModelFactory)
                fragment
            }

            RecipeInsertNewFragment::class.java.name -> {
                val fragment = RecipeInsertNewFragment(viewModelFactory)
                fragment
            }

            SplashFragment::class.java.name -> {
                val fragment = SplashFragment(viewModelFactory)
                fragment
            }


            else -> {
                super.instantiate(classLoader, className)
            }
        }
}