package com.bhuvnesh.diary.di

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.framework.presentation.common.RecipeFragmentFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
object RecipeFragmentFactoryModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        dateUtil: DateUtil
    ): FragmentFactory {
        return RecipeFragmentFactory(
            viewModelFactory,
            dateUtil
        )
    }
}

