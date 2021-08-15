package com.bhuvnesh.diary.framework.presentation

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.bhuvnesh.diary.R
import com.bhuvnesh.diary.business.domain.state.DialogInputCaptureCallback
import com.bhuvnesh.diary.business.domain.state.Response
import com.bhuvnesh.diary.business.domain.state.StateMessageCallback
import com.bhuvnesh.diary.framework.presentation.common.RecipeFragmentFactory
import com.bhuvnesh.diary.util.printLogD
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UIController {


    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var fragmentFactory: RecipeFragmentFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        printLogD("MainActivity", "FirebaseAuth: $firebaseAuth")


    }

    override fun displayProgressBar(isDisplayed: Boolean) {
        //TODO("Not yet implemented")
    }

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager
                .hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    override fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback) {
        // TODO("Not yet implemented")
    }

    override fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        //TODO("Not yet implemented")
    }
}