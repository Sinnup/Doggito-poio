package com.fruse.dogedex.dogDetail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.fruse.dogedex.databinding.ActivityDogDetailBinding

class DogDetailActivity : AppCompatActivity() {

    companion object {
        const val DOG_KEY = "dog"
        const val IS_RECOGNITION_KEY = "is_recognition"
    }

    private val viewModel: DogDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*
                val dog = intent?.extras?.getParcelable<Dog>(DOG_KEY)
                val isRecognition = intent?.extras?.getBoolean(IS_RECOGNITION_KEY, false) ?: false

                if (dog == null) {
                    Toast.makeText(this, R.string.error_showing_dog_not_found, Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                binding.dogIndex.text = getString(R.string.dog_index_format).format(dog.index)
                binding.lifeExpectancy.text =
                    getString(R.string.dog_life_expectancy_format).format(dog.lifeExpectancy)
                binding.dog = dog

                binding.dogImage.load(dog.imageUrl)

                viewModel.status.observe(this) { status ->
                    when (status) {
                        is ApiResponseStatus.Error -> {
                            binding.loadingWheel.isVisible = false
                            Toast.makeText(this, status.messageId, Toast.LENGTH_SHORT).show()
                        }

                        is ApiResponseStatus.Loading -> binding.loadingWheel.isVisible = true
                        is ApiResponseStatus.Success -> {
                            binding.loadingWheel.isVisible = false
                            finish()
                        }
                    }
                }


                binding.closeButton.setOnClickListener {
                    if (isRecognition) {
                        viewModel.addDogToUser(dog.id)
                    } else {
                        finish()
                    }
                }

         */
    }
}