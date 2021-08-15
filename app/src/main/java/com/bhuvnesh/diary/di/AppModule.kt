package com.bhuvnesh.diary.di

import android.content.SharedPreferences
import com.bhuvnesh.diary.business.data.cache.abstraction.RecipeCacheDataSource
import com.bhuvnesh.diary.business.data.cache.implementation.RecipeCacheDataSourceImpl
import com.bhuvnesh.diary.business.data.network.abstraction.RecipeNetworkDataSource
import com.bhuvnesh.diary.business.data.network.implementation.RecipeNetworkDataSourceImpl
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.business.interactors.common.DeleteMultipleRecipes
import com.bhuvnesh.diary.business.interactors.recipeInsertNew.InsertRecipe
import com.bhuvnesh.diary.business.interactors.recipeInsertNew.RecipeInsertNewInteractors
import com.bhuvnesh.diary.business.interactors.recipedetail.RecipeDetailInteractors
import com.bhuvnesh.diary.business.interactors.recipedetail.UpdateRecipe
import com.bhuvnesh.diary.business.interactors.recipelist.DeleteRecipe
import com.bhuvnesh.diary.business.interactors.recipelist.GetNumRecipes
import com.bhuvnesh.diary.business.interactors.recipelist.RecipeListInteractors
import com.bhuvnesh.diary.business.interactors.recipelist.SearchRecipes
import com.bhuvnesh.diary.business.interactors.splash.SyncDeletedRecipes
import com.bhuvnesh.diary.business.interactors.splash.SyncRecipes
import com.bhuvnesh.diary.framework.dataSource.cache.abstraction.RecipeDaoService
import com.bhuvnesh.diary.framework.dataSource.cache.database.RecipeDao
import com.bhuvnesh.diary.framework.dataSource.cache.database.RecipeDatabase
import com.bhuvnesh.diary.framework.dataSource.cache.implementation.RecipeDaoServiceImpl
import com.bhuvnesh.diary.framework.dataSource.cache.mappers.CacheMapper
import com.bhuvnesh.diary.framework.dataSource.network.abstraction.RecipeFirestoreService
import com.bhuvnesh.diary.framework.dataSource.network.implementation.RecipeFirestoreServiceImpl
import com.bhuvnesh.diary.framework.dataSource.network.mappers.NetworkMapper
import com.bhuvnesh.diary.framework.presentation.splash.RecipeNetworkSyncManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@InstallIn(SingletonComponent::class)
@Module
object AppModule {


    // https://developer.android.com/reference/java/text/SimpleDateFormat.html?hl=pt-br
    @JvmStatic
    @Singleton
    @Provides
    fun provideDateFormat(): SimpleDateFormat {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC-7") // match firestore
        return sdf
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDateUtil(dateFormat: SimpleDateFormat): DateUtil {
        return DateUtil(
            dateFormat
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefsEditor(
        sharedPreferences: SharedPreferences
    ): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeFactory(dateUtil: DateUtil): RecipeFactory {
        return RecipeFactory(
            dateUtil
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeDAO(recipeDatabase: RecipeDatabase): RecipeDao {
        return recipeDatabase.recipeDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeCacheMapper(dateUtil: DateUtil): CacheMapper {
        return CacheMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeNetworkMapper(dateUtil: DateUtil): NetworkMapper {
        return NetworkMapper(dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeDaoService(
        recipeDao: RecipeDao,
        recipeEntityMapper: CacheMapper,
        dateUtil: DateUtil
    ): RecipeDaoService {
        return RecipeDaoServiceImpl(recipeDao, recipeEntityMapper, dateUtil)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeCacheDataSource(
        recipeDaoService: RecipeDaoService
    ): RecipeCacheDataSource {
        return RecipeCacheDataSourceImpl(recipeDaoService)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreService(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        networkMapper: NetworkMapper
    ): RecipeFirestoreService {
        return RecipeFirestoreServiceImpl(
            firebaseAuth,
            firebaseFirestore,
            networkMapper
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeNetworkDataSource(
        firestoreService: RecipeFirestoreServiceImpl
    ): RecipeNetworkDataSource {
        return RecipeNetworkDataSourceImpl(
            firestoreService
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncRecipes(
        recipeCacheDataSource: RecipeCacheDataSource,
        recipeNetworkDataSource: RecipeNetworkDataSource
    ): SyncRecipes {
        return SyncRecipes(
            recipeCacheDataSource,
            recipeNetworkDataSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncDeletedRecipes(
        recipeCacheDataSource: RecipeCacheDataSource,
        recipeNetworkDataSource: RecipeNetworkDataSource
    ): SyncDeletedRecipes {
        return SyncDeletedRecipes(
            recipeCacheDataSource,
            recipeNetworkDataSource
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeDetailInteractors(
        recipeCacheDataSource: RecipeCacheDataSource,
        recipeNetworkDataSource: RecipeNetworkDataSource
    ): RecipeDetailInteractors {
        return RecipeDetailInteractors(
            DeleteRecipe(recipeCacheDataSource, recipeNetworkDataSource),
            UpdateRecipe(recipeCacheDataSource, recipeNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeListInteractors(
        recipeCacheDataSource: RecipeCacheDataSource,
        recipeNetworkDataSource: RecipeNetworkDataSource,
    ): RecipeListInteractors {
        return RecipeListInteractors(
            DeleteRecipe(recipeCacheDataSource, recipeNetworkDataSource),
            SearchRecipes(recipeCacheDataSource),
            GetNumRecipes(recipeCacheDataSource),
            DeleteMultipleRecipes(recipeCacheDataSource, recipeNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeInsertNewInteractors(
        recipeCacheDataSource: RecipeCacheDataSource,
        recipeNetworkDataSource: RecipeNetworkDataSource,
        recipeFactory: RecipeFactory
    ): RecipeInsertNewInteractors {
        return RecipeInsertNewInteractors(
            InsertRecipe(recipeCacheDataSource, recipeNetworkDataSource, recipeFactory),
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideRecipeNetworkSyncManager(
        syncRecipes: SyncRecipes,
        syncDeletedRecipes: SyncDeletedRecipes
    ): RecipeNetworkSyncManager {
        return RecipeNetworkSyncManager(syncRecipes, syncDeletedRecipes)
    }

}
