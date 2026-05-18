package com.fruse.dogedex.main

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.fruse.dogedex.auth.auth.LoginActivity
import com.fruse.dogedex.core.api.ApiServiceInterceptor
import com.fruse.dogedex.camera.R
import com.fruse.dogedex.core.model.User
import com.fruse.dogedex.databinding.ActivityMainBinding
import com.fruse.dogedex.dogDetail.DogDetailComposeActivity
import com.fruse.dogedex.dogDetail.DogDetailComposeActivity.Companion.DOG_KEY
import com.fruse.dogedex.dogDetail.DogDetailComposeActivity.Companion.IS_RECOGNITION_KEY
import com.fruse.dogedex.dogDetail.DogDetailComposeActivity.Companion.MOST_PROBABLE_DOG_IDS
import com.fruse.dogedex.dogList.DogListActivity
import com.fruse.dogedex.camera.machinelearning.DogRecognition
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.settings.SettingsActivity
import com.fruse.dogedex.core.testutils.EspressoIdlingResource
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setUpCamera()
        } else {
            Toast.makeText(
                this, getString(R.string.camera_permission_rejected_message), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private var isCameraReady = false
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = User.getLoggedInUser(this)
        if (user == null) {
            openLoginActivity()
            return
        } else {
            ApiServiceInterceptor.setSessionToken(user.authenticationToken)
        }

        binding.settingsFab.setOnClickListener {
            openSettingsActivity()
        }

        binding.dogListFab.setOnClickListener {
            openDogListActivity()
        }

        requestCameraPermission()
    }

    private fun openDogDetailActivity(dog: Dog, probableDogIds: List<String>) {
        val intent = Intent(this, DogDetailComposeActivity::class.java)
        intent.putExtra(DOG_KEY, dog)
        intent.putExtra(MOST_PROBABLE_DOG_IDS, ArrayList(probableDogIds))
        intent.putExtra(IS_RECOGNITION_KEY, true)
        startActivity(intent)
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                setUpCamera()
            }

            shouldShowRequestPermissionRationale(CAMERA) -> {
                AlertDialog.Builder(this).setTitle(getString(R.string.camera_permission_rationale_dialog_title))
                    .setMessage(getString(R.string.camera_permission_rationale_dialog_message))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        requestPermissionLauncher.launch(CAMERA)
                    }.setNegativeButton(android.R.string.cancel) { _, _ ->

                    }.show()
            }

            else -> {
                requestPermissionLauncher.launch(CAMERA)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized) cameraExecutor.shutdown()
    }

    private fun setUpCamera() {
        binding.cameraPreview.post {
            imageCapture =
                ImageCapture.Builder().setTargetRotation(binding.cameraPreview.display.rotation)
                    .build()

            cameraExecutor = Executors.newSingleThreadExecutor()
            startCamera()
            isCameraReady = true
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        EspressoIdlingResource.increment()
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
            imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                EspressoIdlingResource.decrement()
                viewModel.recognizeImage(imageProxy)
            })

            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun enableTakePhotoButton(dogRecognition: DogRecognition) {
        if (dogRecognition.confidence > 70.0) {
            binding.takePhotoFab.alpha = 1f
            binding.takePhotoFab.setOnClickListener {
                viewModel.getDogBYMlId(dogRecognition.id)
            }
        } else {
            binding.takePhotoFab.alpha = 0.2f
            binding.takePhotoFab.setOnClickListener(null)
        }
    }

    private fun openDogListActivity() {
        startActivity(Intent(this, DogListActivity::class.java))
    }

    private fun openSettingsActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
