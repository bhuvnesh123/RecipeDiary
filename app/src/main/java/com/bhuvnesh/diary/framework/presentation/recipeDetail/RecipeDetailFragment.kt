package com.bhuvnesh.diary.framework.presentation.recipeDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bhuvnesh.diary.business.domain.model.Recipe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

//const val RECIPE_DETAIL_STATE_BUNDLE_KEY =
//    "com.bhuvnesh.diary.recipes.framework.presentation.recipedetail.state"

@FlowPreview
@ExperimentalCoroutinesApi
class RecipeDetailFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

            val args = arguments?.let { RecipeDetailFragmentArgs.fromBundle(it) }

            setContent { args?.bundleRecipe?.let { RecipeDetail(it) } }
        }
    }


    @Composable
    fun RecipeDetail(recipe: Recipe) {
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = {
                    Text(text = "Recipe Details")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val action =
                            RecipeDetailFragmentDirections.actionRecipeDetailFragmentToRecipeListFragment()
                        findNavController().navigate(action)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                backgroundColor = Color.Blue,
                contentColor = Color.White,
                elevation = 2.dp
            )
        }, content = { RecipeDetailContent(recipe) })

    }

    @Composable
    fun RecipeDetailContent(recipe: Recipe) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Text(
                text = recipe.title,
                style = TextStyle(fontSize = 20.sp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ingredients",
                style = TextStyle(fontSize = 20.sp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = recipe.ingredients,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Preparation",
                style = TextStyle(fontSize = 20.sp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            val steps = recipe.steps.split("\n")

//            LazyColumn(
//                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(
//                    count = steps.size,
//                    itemContent = {
//                        Card(
//                            modifier = Modifier
//                                .fillMaxWidth(),
//                            elevation = 2.dp,
//                            backgroundColor = Color(0xffdbffdb),
//                            contentColor = Color.Black
//                        ) {
//                            Text(
//                                text = steps[it],
//                                style = TextStyle(fontSize = 14.sp),
//                                modifier = Modifier
//                                    .padding(8.dp)
//                                    .align(Alignment.Start)
//                            )
//                        }
//
//                    })
//            }
            for (item in steps) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = 2.dp,
                    backgroundColor = Color(0xffdbffdb),
                    contentColor = Color.Black
                ) {
                    Text(
                        color = Color.Black,
                        text = item,
                        style = TextStyle(fontSize = 16.sp),
                        modifier = Modifier
                            .align(Alignment.Start)
                    )
                }
            }

        }


    }


}
