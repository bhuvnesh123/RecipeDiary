package com.bhuvnesh.diary.framework.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bhuvnesh.diary.framework.dataSource.network.implementation.RecipeFirestoreServiceImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class SplashFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment() {

    private val viewModel: SplashViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent { Splash() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (RecipeFirestoreServiceImpl.USER_ID.equals("XXXXXXXXXXXXXXXXXXXXXXXXXXX")) {
            Toast.makeText(
                context,
                "Please configure Firebase as per project's README.md on Github",
                Toast.LENGTH_LONG
            ).show()
        } else {
            subscribeObservers()
        }
    }


    private fun subscribeObservers() {

        viewModel.hasSyncBeenExecuted()
            .observe(viewLifecycleOwner, { hasSyncBeenExecuted ->

                if (hasSyncBeenExecuted) {
                    navRecipeListListFragment()
                }
            })
    }

    @Composable
    fun Splash() {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Fastfood,
                contentDescription = "Recipe"
            )
            Text(
                text = "Recipe Dairy",
                style = TextStyle(fontSize = 18.sp),
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }

    private fun navRecipeListListFragment() {
        val action =
            SplashFragmentDirections
                .actionSplashFragmentToRecipeListFragment()
        findNavController().navigate(action)
    }

}