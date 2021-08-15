package com.bhuvnesh.diary.framework.presentation.recipeList


import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bhuvnesh.diary.R
import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.business.domain.state.UIComponentType
import com.bhuvnesh.diary.business.domain.utils.DateUtil
import com.bhuvnesh.diary.framework.presentation.recipeList.components.AnimatedSwipeDismiss
import com.bhuvnesh.diary.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
class RecipeListFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
) : Fragment() {

    val viewModel: RecipeListViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()
    }

    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent { BoxLayout() }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.recipeList?.let {
                if (viewModel.isPaginationExhausted()
                    && !viewModel.isQueryExhausted()
                ) {
                    viewModel.setQueryExhausted(true)
                }
                viewModel.recipes.value = it
            }

        })

        viewModel.stateMessage.observe(viewLifecycleOwner, { stateMessage ->
            stateMessage?.let { message ->
                when (message.response.uiComponentType) {
                    is UIComponentType.SnackBar -> {
                        message.response.message?.let { viewModel.snackbarText.value = it }
                        viewModel.snackbarVisibleState.value = true
                    }
                    is UIComponentType.Toast -> {
                        Toast.makeText(context, message.response.message, Toast.LENGTH_LONG).show()
                        viewModel.clearStateMessage()
                    }
                    is UIComponentType.None -> {
                        viewModel.clearStateMessage()
                    }
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()

        viewModel.retrieveNumRecipesInCache()
        viewModel.clearList()
        viewModel.refreshSearchQuery()
    }

    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @Composable
    fun BoxLayout() {
        val fabShape = RoundedCornerShape(50)
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {},
                    // We specify the same shape that we passed as the cutoutShape above.
                    shape = fabShape,
                    // We use the secondary color from the current theme. It uses the defaults when
                    // you don't specify a theme (this example doesn't specify a theme either hence
                    // it will just use defaults. Look at DarkModeActivity if you want to see an
                    // example of using themes.
                    backgroundColor = MaterialTheme.colors.secondary
                ) {
                    IconButton(onClick = {
                        val action =
                            RecipeListFragmentDirections
                                .actionRecipeListFragmentToRecipeInsertNewFragment()
                        findNavController().navigate(action)
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Recipe")
                    }
                }
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    RecipesList(viewModel.recipes.value)
                    Snackbar()
                }
            })
    }

    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    @Composable
    fun RecipesList(
        recipes: List<Recipe>
    ) {
        val query = viewModel.query.value

        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = Color.Blue,
                elevation = 8.dp,
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val keyboardController = LocalSoftwareKeyboardController.current

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(.9f)
                            .padding(8.dp),
                        value = query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        label = {
                            Text(
                                text = "Search", color = Color.White
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                        ),
                        leadingIcon = {
                            Icon(Icons.Filled.Search, "print") // ok
                        },
                        textStyle = TextStyle(color = Color.White),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.setQuery(query)
                            startNewSearch()
                            keyboardController?.hide()
                        })
                    )
                    ConstraintLayout(
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        val (menu) = createRefs()
                        IconButton(
                            modifier = Modifier
                                .constrainAs(menu) {
                                    end.linkTo(parent.end)
                                    linkTo(top = parent.top, bottom = parent.bottom)
                                },
                            onClick = { viewModel.shouldDialogOpen.value = true },
                        ) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "Dialog",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            // Use LazyRow when making horizontal lists
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = recipes.size,
                    itemContent = { it ->
                        AnimatedSwipeDismiss(
                            item = recipes[it],
                            background = { isDismissed ->

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color = Color.Red)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
//                                    val alpha = if (isDismissed) {
//                                        animate(targetValue =0f ,initialValue = 1f)
//                                    } else animate(targetValue =1f ,initialValue = 0f)

                                    Icon(
                                        Icons.Filled.Delete,
                                        tint = Color.White,
                                        contentDescription = "Delete"
                                    )
                                }
                            },
                            content = { isDismissed -> RecipeistItem(recipe = recipes[it]) },
                            onDismiss = {
                                viewModel.beginPendingDelete(it)
                            }
                        )

                    })
            }
            AlertDialogComponent()
        }

    }

    // The UI for each list item can be generated by a reusable composable
    @ExperimentalMaterialApi
    @Composable
    fun RecipeistItem(recipe: Recipe) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = 2.dp,
            backgroundColor = Color(0xffdbffdb),
            contentColor = Color.Black,
            onClick = {
                val action =
                    RecipeListFragmentDirections
                        .actionRecipeListFragmentToRecipeDetailFragment(recipe)
                findNavController().navigate(action)
            }
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                FastFoodImage()
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = recipe.title,
                        style = TextStyle(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.Start)
                    )
                    Text(
                        text = recipe.updated_at.plus(" GMT"),
                        style = TextStyle(fontSize = 14.sp),
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.Start)
                    )
                }


            }
        }
    }

    @Composable
    private fun FastFoodImage() {
        Image(
            contentDescription = "Recipe",
            painter = painterResource(R.drawable.ic_baseline_fastfood_24),
            modifier = Modifier
                .padding(8.dp)
        )
    }

    @Composable
    fun AlertDialogComponent() {
        val context = LocalContext.current
        val radioOptions = listOf("Title", "Date")
        val sortOptions = listOf("Ascending", "Descending")


        if (viewModel.shouldDialogOpen.value) {
            AlertDialog(onDismissRequest = { viewModel.shouldDialogOpen.value = false },
                text = {
                    Column {
                        Text(
                            text = "Filter by Title Or Date",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        radioOptions.forEach { text ->
                            Row(
                                Modifier
                                    // using modifier to add max
                                    // width to our radio button.
                                    .fillMaxWidth()
                                    // below method is use to add
                                    // selectable to our radio button.
                                    .selectable(
                                        // this method is called when
                                        // radio button is selected.
                                        selected = (text == viewModel.selectedFilter.value),
                                        onClick = { viewModel.onFilterSelected(text) }
                                        // below method is called on
                                        // clicking of radio button.
                                    )
                                    // below line is use to add
                                    // padding to radio button.
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ) {
                                // below line is use to get context.

                                // below line is use to
                                // generate radio button
                                RadioButton(
                                    // inside this method we are
                                    // adding selected with a option.
                                    selected = (text == viewModel.selectedFilter.value),
                                    onClick = {
                                        // inide on click method we are setting a
                                        // selected option of our radio buttons.
                                        viewModel.onFilterSelected(text)

                                    },
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .alignBy(FirstBaseline)

                                )
                                // below line is use to add
                                // text to our radio buttons.
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.body1.merge(),
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .alignBy(FirstBaseline)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Sort by Ascending Or Descending",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        sortOptions.forEach { text ->
                            Row(
                                Modifier
                                    // using modifier to add max
                                    // width to our radio button.
                                    .fillMaxWidth()
                                    // below method is use to add
                                    // selectable to our radio button.
                                    .selectable(
                                        // this method is called when
                                        // radio button is selected.
                                        selected = (text == viewModel.selectedSort.value),
                                        onClick = { viewModel.onSortSelected(text) }
                                        // below method is called on
                                        // clicking of radio button.
                                    )
                                    // below line is use to add
                                    // padding to radio button.
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ) {
                                // below line is use to get context.

                                // below line is use to
                                // generate radio button
                                RadioButton(
                                    // inside this method we are
                                    // adding selected with a option.
                                    selected = (text == viewModel.selectedSort.value),
                                    onClick = {
                                        // inide on click method we are setting a
                                        // selected option of our radio buttons.
                                        viewModel.onSortSelected(text)

                                    },
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .alignBy(FirstBaseline)

                                )
                                // below line is use to add
                                // text to our radio buttons.
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.body1.merge(),
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .alignBy(FirstBaseline)
                                )
                            }
                        }
                    }
                },


                confirmButton = {
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {
                        viewModel.shouldDialogOpen.value = false
                    }) {
                        Text(text = "Okay")
                    }
                }
            )
        }

    }

    @Composable
    fun Snackbar() {
        Row(modifier = Modifier.fillMaxSize()) {


            if (viewModel.snackbarVisibleState.value) {
                Snackbar(
                    action = {
                        Button(onClick = {
                            viewModel.snackbarText.value = ""
                            viewModel.snackbarVisibleState.value = false
                            viewModel.clearStateMessage()
                        }) {
                            Text("Okay")
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Bottom)
                ) {
                    Text(viewModel.snackbarText.value)
                    viewModel.clearStateMessage()
                    object : CountDownTimer(10000, 1000) {

                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            viewModel.snackbarText.value = ""
                            viewModel.snackbarVisibleState.value = false
                            viewModel.clearStateMessage()
                        }
                    }.start()
                }
            }
        }
    }

    private fun startNewSearch() {
        printLogD("DCM", "start new search")
        viewModel.clearList()
        viewModel.loadFirstPage()
    }


}









































