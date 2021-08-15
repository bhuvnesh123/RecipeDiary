package com.bhuvnesh.diary.framework.presentation

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4ClassRunner::class)
@HiltAndroidTest
class TempTest {


    @Inject
    lateinit var firebaseFirestore: FirebaseFirestore

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun someRandomTest() {

        assert(::firebaseFirestore.isInitialized)
    }

}
