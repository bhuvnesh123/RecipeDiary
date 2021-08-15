package com.bhuvnesh.diary.framework.presentation

import com.bhuvnesh.diary.business.domain.state.DialogInputCaptureCallback
import com.bhuvnesh.diary.business.domain.state.Response
import com.bhuvnesh.diary.business.domain.state.StateMessageCallback


interface UIController {

    fun displayProgressBar(isDisplayed: Boolean)

    fun hideSoftKeyboard()

    fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback)

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )

}


















