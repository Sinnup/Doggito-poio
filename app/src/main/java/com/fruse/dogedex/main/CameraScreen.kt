package com.fruse.dogedex.main

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fruse.dogedex.core.composables.ErrorDialog
import com.fruse.dogedex.core.composables.LoadingWheel
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.core.testutils.EspressoIdlingResource
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToDogDetail: (dog: Dog, probableDogIds: List<String>, isRecognition: Boolean) -> Unit,
    onNavigateToDogList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.recognizedDog) {
        val dog = uiState.recognizedDog
        if (dog != null) {
            onNavigateToDogDetail(dog, uiState.probableDogIds, true)
            viewModel.onDogDetailNavigated()
        }
    }

    CameraContent(
        uiState = uiState,
        onRecognizeImage = viewModel::recognizeImage,
        onGetDogByMlId = viewModel::getDogBYMlId,
        onDismissError = viewModel::dismissError,
        onNavigateToDogList = onNavigateToDogList,
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
private fun CameraContent(
    uiState: MainUiState,
    onRecognizeImage: (androidx.camera.core.ImageProxy) -> Unit,
    onGetDogByMlId: (String) -> Unit,
    onDismissError: () -> Unit,
    onNavigateToDogList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isCameraReady by remember { mutableStateOf(false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    val previewView = remember { PreviewView(context) }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        EspressoIdlingResource.increment()
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val capture = ImageCapture.Builder()
                .setTargetRotation(previewView.display?.rotation ?: 0)
                .build()
            imageCapture.value = capture

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                EspressoIdlingResource.decrement()
                onRecognizeImage(imageProxy)
            })

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, capture, imageAnalysis
            )
            isCameraReady = true
        }, ContextCompat.getMainExecutor(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(context, CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                permissionLauncher.launch(CAMERA)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        val dogRecognition = uiState.dogRecognition
        val isHighConfidence = (dogRecognition?.confidence ?: 0f) > 70f

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            onClick = {
                if (isCameraReady && isHighConfidence && dogRecognition != null) {
                    onGetDogByMlId(dogRecognition.id)
                }
            }
        ) {
            Icon(
                painter = painterResource(id = com.fruse.dogedex.R.drawable.ic_baseline_photo_camera),
                contentDescription = null
            )
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 16.dp),
            onClick = onNavigateToDogList
        ) {
            Icon(
                imageVector = Icons.Filled.List,
                contentDescription = null
            )
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 32.dp, start = 16.dp),
            onClick = onNavigateToSettings
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null
            )
        }

        if (uiState.isLoading) {
            LoadingWheel()
        } else if (uiState.error != null) {
            ErrorDialog(messageId = uiState.error) {
                onDismissError()
            }
        }
    }
}
