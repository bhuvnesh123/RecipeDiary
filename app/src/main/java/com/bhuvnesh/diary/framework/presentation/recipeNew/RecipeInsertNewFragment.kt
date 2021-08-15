package com.bhuvnesh.diary.framework.presentation.recipeNew

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bhuvnesh.diary.R
import com.bhuvnesh.diary.business.domain.state.UIComponentType
import com.bhuvnesh.diary.framework.presentation.MainActivity
import com.bhuvnesh.diary.framework.presentation.UIController
import com.bhuvnesh.diary.framework.presentation.recipeNew.state.RecipeInsertNewStateEvent

class RecipeInsertNewFragment constructor(private val viewModelFactory: ViewModelProvider.Factory) :
    Fragment() {
    private val viewModel: RecipeInsertViewModel by viewModels {
        viewModelFactory
    }
    private lateinit var uiController: UIController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()
        activity?.let {
            if (it is MainActivity) {
                try {
                    uiController = context as UIController
                } catch (e: ClassCastException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @ExperimentalComposeUiApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent { InsertRecipe() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
    }

    @Composable
    fun InsertRecipe() {
        val title = viewModel.title.value
        val ingredients = viewModel.ingredients.value

        MaterialTheme {
            Column {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = "Create New Recipe")
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    val action =
                                        RecipeInsertNewFragmentDirections
                                            .actionRecipeInsertNewFragmentToRecipeListFragment()
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
                            elevation = 2.dp,
                            actions = {
                                IconButton(onClick = {
                                    uiController.hideSoftKeyboard()

                                    if (TextUtils.isEmpty(viewModel.title.value)) {
                                        viewModel.snackbarText.value = "Please Enter Title"
                                        viewModel.snackbarVisibleState.value = true

                                    } else if (viewModel.stepsList.size == 0) {
                                        viewModel.snackbarText.value = "Please Enter Steps"
                                        viewModel.snackbarVisibleState.value = true

                                    } else {
                                        viewModel.setStateEvent(
                                            RecipeInsertNewStateEvent.InsertNewRecipeEvent(
                                                viewModel.title.value,
                                                viewModel.ingredients.value,
                                                viewModel.convertStepListToString(),
                                                null
                                            )
                                        )
                                    }
                                }) {
                                    Icon(
                                        contentDescription = "Post",
                                        painter = painterResource(R.drawable.ic_baseline_done_24)
                                    )
                                }
                            }
                        )
                    }, content = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = title,
                                    onValueChange = {
                                        viewModel.onTitleChanged(it)
                                    },
                                    label = {
                                        Text(text = "Title")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = TextFieldDefaults.textFieldColors(
                                        backgroundColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = ingredients,
                                    onValueChange = {
                                        viewModel.onIngredientsChanged(it)
                                    },
                                    label = {
                                        Text(text = "Ingredients")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = TextFieldDefaults.textFieldColors(
                                        backgroundColor = Color.Transparent
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                repeat(viewModel.textFieldCount.value) { stepNo ->
                                    val textState = remember { mutableStateOf(TextFieldValue()) }



                                    OutlinedTextField(
                                        modifier = Modifier.fillMaxWidth(),
                                        value = textState.value,
                                        onValueChange = {
                                            textState.value = it
                                            viewModel.setSteps(stepNo.plus(1), it.text)
                                        },
                                        label = {
                                            Text(text = "Step ".plus(stepNo.plus(1)).plus("..."))
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = TextFieldDefaults.textFieldColors(
                                            backgroundColor = Color.Transparent
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = {
                                    viewModel.textFieldCount.value++
                                }) {
                                    Text("Add Steps")
                                }

                            }
                            Snackbar()
                        }
                    })


            }
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
                        }) {
                            Text("Ok")
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Bottom)
                ) {
                    Text(viewModel.snackbarText.value)
                    viewModel.clearStateMessage()
                }
            }
        }
    }

    private fun subscribeObservers() {
        viewModel.stateMessage.observe(viewLifecycleOwner, { stateMessage ->
            stateMessage?.let { message ->
                when (message.response.uiComponentType) {
                    is UIComponentType.SnackBar -> {
                        message.response.message?.let { msg ->
                            viewModel.snackbarText.value = msg
                            viewModel.snackbarVisibleState.value = true
                        }
                    }
                    is UIComponentType.Toast -> {
                        message.response.message?.let { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            viewModel.clearStateMessage()
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState?.newRecipe?.let {
                val action =
                    RecipeInsertNewFragmentDirections
                        .actionRecipeInsertNewFragmentToRecipeListFragment()
                findNavController().navigate(action)
            }

        })
        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, {
            uiController.displayProgressBar(it)
        })
    }
}
