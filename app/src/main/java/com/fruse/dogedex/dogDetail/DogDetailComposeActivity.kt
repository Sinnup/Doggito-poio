package com.fruse.dogedex.dogDetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fruse.dogedex.core.ui.theme.DogedexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DogDetailComposeActivity : ComponentActivity() {

    companion object {
        const val DOG_KEY = "dog"
        const val IS_RECOGNITION_KEY = "is_recognition"
        const val MOST_PROBABLE_DOG_IDS = "most_probable_dog_ids"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            DogedexTheme {
                DogDetailScreen(
                    finishActivity = {
                        finish()
                    }
                )
            }
        }
    }
}
