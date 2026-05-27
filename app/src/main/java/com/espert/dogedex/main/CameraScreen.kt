package com.espert.dogedex.main

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.espert.dogedex.ui.isExpanded
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espert.dogedex.core.composables.ErrorDialog
import com.espert.dogedex.core.composables.LoadingWheel
import com.espert.dogedex.core.model.Dog
import com.espert.dogedex.core.testutils.EspressoIdlingResource
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    windowSizeClass: WindowSizeClass,
    onNavigateToDogDetail: (dog: Dog, probableDogIds: List<String>) -> Unit,
    onNavigateToDogList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MainUiEffect.NavigateToDogDetail ->
                    onNavigateToDogDetail(effect.dog, effect.probableDogIds)

                is MainUiEffect.NavigateToDogList -> onNavigateToDogList()
                is MainUiEffect.NavigateToSettings -> onNavigateToSettings()
            }
        }
    }

    CameraContent(
        windowSizeClass = windowSizeClass,
        uiState = uiState,
        onRecognizeImage = { viewModel.handleAction(MainUiAction.RecognizeImage(it)) },
        onGetDogByMlId = { viewModel.handleAction(MainUiAction.GetDogByMlId(it)) },
        onDismissError = { viewModel.handleAction(MainUiAction.DismissError) },
        onNavigateToDogList = { viewModel.handleAction(MainUiAction.NavigateToDogList) },
        onNavigateToSettings = { viewModel.handleAction(MainUiAction.NavigateToSettings) },
    )
}

@Composable
private fun CameraContent(
    windowSizeClass: WindowSizeClass,
    uiState: MainUiState,
    onRecognizeImage: (androidx.camera.core.ImageProxy) -> Unit,
    onGetDogByMlId: (String) -> Unit,
    onDismissError: () -> Unit,
    onNavigateToDogList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isCameraReady by remember { mutableStateOf(value = false) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    val previewView = remember { PreviewView(context) }
    val camera = remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        EspressoIdlingResource.increment()
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                preview.surfaceProvider = previewView.surfaceProvider
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val capture = ImageCapture.Builder()
                    .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                    .build()
                imageCapture.value = capture

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                    .build()
                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    EspressoIdlingResource.decrement()
                    onRecognizeImage(imageProxy)
                }

                cameraProvider.unbindAll()
                camera.value = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, capture, imageAnalysis
                )
                isCameraReady = true
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) startCamera()
    }

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, CAMERA) -> {
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
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val point = previewView.meteringPointFactory
                            .createPoint(offset.x, offset.y)
                        val action = FocusMeteringAction.Builder(point).build()
                        camera.value?.cameraControl?.startFocusAndMetering(action)
                    }
                }
        )

        val dogRecognition = uiState.dogRecognition
        val isHighConfidence = (dogRecognition?.confidence ?: 0f) > 70f

        if (windowSizeClass.isExpanded) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .navigationBarsPadding()
                    .padding(end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isHighConfidence) {
                    FloatingActionButton(
                        modifier = Modifier.semantics { testTag = "take-photo-fab" },
                        onClick = {
                            if (isCameraReady && (dogRecognition != null)) {
                                onGetDogByMlId(dogRecognition.id)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = com.espert.dogedex.R.drawable.ic_baseline_photo_camera),
                            contentDescription = null
                        )
                    }
                }
                FloatingActionButton(
                    modifier = Modifier.semantics { testTag = "dog-list-fab" },
                    onClick = onNavigateToDogList
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null
                    )
                }
            }
        } else {
            if (isHighConfidence) {
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 32.dp)
                        .semantics { testTag = "take-photo-fab" },
                    onClick = {
                        if (isCameraReady && (dogRecognition != null)) {
                            onGetDogByMlId(dogRecognition.id)
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = com.espert.dogedex.R.drawable.ic_baseline_photo_camera),
                        contentDescription = null
                    )
                }
            }

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp, end = 16.dp)
                    .semantics { testTag = "dog-list-fab" },
                onClick = onNavigateToDogList
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null
                )
            }
        }

//        FloatingActionButton(
//            modifier = Modifier
//                .align(Alignment.BottomStart)
//                .navigationBarsPadding()
//                .padding(bottom = 32.dp, start = 16.dp)
//                .semantics { testTag = "settings-fab" },
//            onClick = onNavigateToSettings
//        ) {
//            Icon(
//                imageVector = Icons.Filled.Settings,
//                contentDescription = null
//            )
//        }

        if (uiState.isLoading) {
            LoadingWheel()
        } else uiState.error?.let {
            ErrorDialog(message = it) {
                onDismissError()
            }
        }
    }
}
