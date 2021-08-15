package com.bhuvnesh.diary.framework.dataSource.cache

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.model.RecipeFactory
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.framework.dataSource.cache.abstraction.RecipeDaoService
import com.bhuvnesh.diary.framework.dataSource.cache.database.RecipeDao
import com.bhuvnesh.diary.framework.dataSource.cache.implementation.RecipeDaoServiceImpl
import com.bhuvnesh.diary.framework.dataSource.cache.mappers.CacheMapper
import com.bhuvnesh.diary.framework.dataSource.data.RecipesDataFactory
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


// runBlockingTest doesn't work:
// https://github.com/Kotlin/kotlinx.coroutines/issues/1204

/*
    LEGEND:
    1. CBS = "Confirm by searching"
    Test cases:
    1. confirm database recipe empty to start (should be test data inserted from CacheTest.kt)
    2. insert a new recipe, CBS
    3. insert a list of recipes, CBS
    4. insert 1000 new recipes, confirm filtered search query works correctly
    5. insert 1000 new recipes, confirm db size increased
    6. delete new recipe, confirm deleted
    7. delete list of recipes, CBS
    8. update a recipe, confirm updated
    9. search recipes, order by date (ASC), confirm order
    10. search recipes, order by date (DESC), confirm order
    11. search recipes, order by title (ASC), confirm order
    12. search recipes, order by title (DESC), confirm order
 */
@ExperimentalCoroutinesApi
@FlowPreview
@RunWith(AndroidJUnit4ClassRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@HiltAndroidTest
class RecipeDaoServiceTests {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    // system in test
    private lateinit var recipeDaoService: RecipeDaoService

    // dependencies
    @Inject
    lateinit var dao: RecipeDao

    @Inject
    lateinit var recipeDataFactory: RecipesDataFactory

    @Inject
    lateinit var dateUtil: DateUtil

    @Inject
    lateinit var recipeFactory: RecipeFactory

    @Inject
    lateinit var cacheMapper: CacheMapper


    @Before
    fun init() {
        hiltRule.inject()
        insertTestData()
        recipeDaoService = RecipeDaoServiceImpl(
            recipeDao = dao,
            recipeMapper = cacheMapper,
            dateUtil = dateUtil
        )
    }


    fun insertTestData() = runBlocking {
        val entityList = cacheMapper.recipeListToEntityList(
            recipeDataFactory.produceListOfRecipes()
        )
        dao.insertRecipes(entityList)
    }

    /**
     * This test runs first. Check to make sure the test data was inserted from
     * CacheTest class.
     */
    @Test
    fun a_searchRecipes_confirmDbNotEmpty() = runBlocking {

        val numRecipes = recipeDaoService.getNumRecipes()

        assertTrue { numRecipes > 0 }

    }

    @Test
    fun insertRecipe_CBS() = runBlocking {

        val newRecipe = recipeFactory.createSingleRecipe(
            null,
            "Super cool title",
            "Some content for the recipe", "test", "test"
        )
        recipeDaoService.insertRecipe(newRecipe)

        val recipes = recipeDaoService.getAllRecipes()
        assert(recipes.contains(newRecipe))
    }

    @Test
    fun insertRecipeList_CBS() = runBlocking {

        val recipeList = recipeFactory.createRecipeList(10)
        recipeDaoService.insertRecipes(recipeList)

        val queriedRecipes = recipeDaoService.getAllRecipes()

        assertTrue { queriedRecipes.containsAll(recipeList) }
    }

    @Test
    fun insert1000Recipes_confirmNumRecipesInDb() = runBlocking {
        val currentNumRecipes = recipeDaoService.getNumRecipes()

        // insert 1000 recipes
        val recipeList = recipeFactory.createRecipeList(1000)
        recipeDaoService.insertRecipes(recipeList)

        val numRecipes = recipeDaoService.getNumRecipes()
        assertEquals(currentNumRecipes + 1000, numRecipes)
    }

    @Test
    fun insert1000Recipes_searchRecipesByTitle_confirm50ExpectedValues() = runBlocking {

        // insert 1000 recipes
        val recipeList = recipeFactory.createRecipeList(1000)
        recipeDaoService.insertRecipes(recipeList)

        // query 50 recipes by specific title
        repeat(50) {
            val randomIndex = Random.nextInt(0, recipeList.size - 1)
            val result = recipeDaoService.searchRecipesOrderByTitleASC(
                query = recipeList.get(randomIndex).title,
                page = 1,
                pageSize = 1
            )
            assertEquals(recipeList.get(randomIndex).title, result.get(0).title)
        }
    }


    @Test
    fun insertRecipe_deleteRecipe_confirmDeleted() = runBlocking {
        val newRecipe = recipeFactory.createSingleRecipe(
            null,
            "Super cool title",
            "Some content for the recipe", "test",
            null,

            )
        recipeDaoService.insertRecipe(newRecipe)

        var recipes = recipeDaoService.getAllRecipes()
        assert(recipes.contains(newRecipe))

        recipeDaoService.deleteRecipe(newRecipe.id)
        recipes = recipeDaoService.getAllRecipes()
        assert(!recipes.contains(newRecipe))
    }

    @Test
    fun deleteRecipeList_confirmDeleted() = runBlocking {
        val recipeList: ArrayList<Recipe> = ArrayList(recipeDaoService.getAllRecipes())

        // select some random recipes for deleting
        val recipesToDelete: ArrayList<Recipe> = ArrayList()

        // 1st
        var recipeToDelete = recipeList.get(Random.nextInt(0, recipeList.size - 1) + 1)
        recipeList.remove(recipeToDelete)
        recipesToDelete.add(recipeToDelete)

        // 2nd
        recipeToDelete = recipeList.get(Random.nextInt(0, recipeList.size - 1) + 1)
        recipeList.remove(recipeToDelete)
        recipesToDelete.add(recipeToDelete)

        // 3rd
        recipeToDelete = recipeList.get(Random.nextInt(0, recipeList.size - 1) + 1)
        recipeList.remove(recipeToDelete)
        recipesToDelete.add(recipeToDelete)

        // 4th
        recipeToDelete = recipeList.get(Random.nextInt(0, recipeList.size - 1) + 1)
        recipeList.remove(recipeToDelete)
        recipesToDelete.add(recipeToDelete)

        recipeDaoService.deleteRecipes(recipesToDelete)

        // confirm they were deleted
        val searchResults = recipeDaoService.getAllRecipes()
        assertFalse { searchResults.containsAll(recipesToDelete) }
    }

    @Test
    fun insertRecipe_updateRecipe_confirmUpdated() = runBlocking {
        val newRecipe = recipeFactory.createSingleRecipe(
            null,
            "Super cool title",
            "Some content for the recipe", "test", "test"
        )
        recipeDaoService.insertRecipe(newRecipe)

        val newTitle = UUID.randomUUID().toString()
        val newIngrediennts = UUID.randomUUID().toString()
        recipeDaoService.updateRecipe(
            primaryKey = newRecipe.id,
            newTitle = newTitle,
            newIngredients = newIngrediennts,
            newSteps = UUID.randomUUID().toString(),
            newImageUrl = UUID.randomUUID().toString(),
            timeStamp = null
        )

        val recipes = recipeDaoService.getAllRecipes()

        var foundRecipe = false
        for (recipe in recipes) {
            if (recipe.id.equals(newRecipe.id)) {
                foundRecipe = true
                assertEquals(newRecipe.id, recipe.id)
                assertEquals(newTitle, recipe.title)
                assertEquals(newIngrediennts, recipe.ingredients)
                assert(newRecipe.updated_at != recipe.updated_at)
                assertEquals(
                    newRecipe.created_at,
                    recipe.created_at
                )
                break
            }
        }
        assertTrue { foundRecipe }
    }

    @Test
    fun searchRecipes_orderByDateASC_confirmOrder() = runBlocking {
        val recipeList = recipeDaoService.searchRecipesOrderByDateASC(
            query = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previousRecipeDate = recipeList.get(0).updated_at
        for (index in 1..recipeList.size - 1) {
            val currentRecipeDate = recipeList.get(index).updated_at
            assertTrue { currentRecipeDate >= previousRecipeDate }
            previousRecipeDate = currentRecipeDate
        }
    }


    @Test
    fun searchRecipes_orderByDateDESC_confirmOrder() = runBlocking {
        val recipeList = recipeDaoService.searchRecipesOrderByDateDESC(
            query = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = recipeList.get(0).updated_at
        for (index in 1..recipeList.size - 1) {
            val current = recipeList.get(index).updated_at
            assertTrue { current <= previous }
            previous = current
        }
    }

    @Test
    fun searchRecipes_orderByTitleASC_confirmOrder() = runBlocking {
        val recipeList = recipeDaoService.searchRecipesOrderByTitleASC(
            query = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = recipeList.get(0).title
        for (index in 1..recipeList.size - 1) {
            val current = recipeList.get(index).title

            assertTrue {
                listOf(previous, current)
                    .asSequence()
                    .zipWithNext { a, b ->
                        a <= b
                    }.all { it }
            }
            previous = current
        }
    }

    @Test
    fun searchRecipes_orderByTitleDESC_confirmOrder() = runBlocking {
        val recipeList = recipeDaoService.searchRecipesOrderByTitleDESC(
            query = "",
            page = 1,
            pageSize = 100
        )

        // check that the date gets larger (newer) as iterate down the list
        var previous = recipeList.get(0).title
        for (index in 1..recipeList.size - 1) {
            val current = recipeList.get(index).title

            assertTrue {
                listOf(previous, current)
                    .asSequence()
                    .zipWithNext { a, b ->
                        a >= b
                    }.all { it }
            }
            previous = current
        }
    }
}
