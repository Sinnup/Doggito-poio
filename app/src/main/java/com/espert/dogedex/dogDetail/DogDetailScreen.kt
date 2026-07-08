package com.espert.dogedex.dogDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.espert.dogedex.R
import com.espert.dogedex.core.composables.ErrorDialog
import com.espert.dogedex.core.composables.LoadingWheel
import com.espert.dogedex.core.model.Dog
import com.espert.dogedex.core.ui.theme.DogedexTheme
import com.espert.dogedex.ui.isCompact
import com.espert.dogedex.ui.isExpanded
import com.espert.dogedex.ui.isMedium
import java.io.File

@OptIn(ExperimentalCoilApi::class)
@Composable
fun DogDetailScreen(
    windowSizeClass: WindowSizeClass,
    dog: Dog,
    onNavigateBack: () -> Unit,
    viewModel: DogDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DogDetailUiEffect.DogAdded -> onNavigateBack()
                is DogDetailUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    LaunchedEffect(uiState.hasDogBeenAdded) {
        if (uiState.hasDogBeenAdded) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.dog_added_to_collection),
                android.widget.Toast.LENGTH_SHORT
            ).show()
            onNavigateBack()
        }
    }

    DogDetailContent(
        windowSizeClass = windowSizeClass,
        uiState = uiState,
        dog = uiState.dog ?: dog,
        onAction = viewModel::handleAction
    )
}

@Composable
private fun DogDetailContent(
    windowSizeClass: WindowSizeClass,
    uiState: DogDetailUiState,
    dog: Dog,
    onAction: (DogDetailUiAction) -> Unit
) {
    val probableDogsDialogEnabled = remember { mutableStateOf(false) }

    val contentMaxWidth = when {
        windowSizeClass.isExpanded -> 600.dp
        windowSizeClass.isMedium -> 480.dp
        else -> Dp.Unspecified
    }
    val imageWidth = if (windowSizeClass.isCompact) 270.dp else 320.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 8.dp, bottom = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val contentModifier = if (contentMaxWidth != Dp.Unspecified) {
            Modifier.widthIn(max = contentMaxWidth)
        } else {
            Modifier
        }
        Box(
            modifier = contentModifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            val topPadding = screenHeight * 0.25f // 1/4 of the s

            DogInformation(dog, uiState.isRecognition, topPadding) {
                onAction(DogDetailUiAction.LoadProbableDogs)
                probableDogsDialogEnabled.value = true
            }
            val filesDir = LocalContext.current.filesDir
            Image(
                modifier = Modifier
                    .width(imageWidth)
                    .safeContentPadding()
                    .padding(top = topPadding - 100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(filesDir, "images/${dog.imageUrl}.jpg"))
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_baseline_photo_camera)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = dog.name
            )
        }

        FloatingActionButton(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .semantics { testTag = "close-details-screen-fab" },
            onClick = {
                if (uiState.isRecognition) {
                    onAction(DogDetailUiAction.AddDogToUser)
                } else {
                    onAction(DogDetailUiAction.NavigateBack)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(
                    if (uiState.isRecognition) R.string.cd_add_to_collection
                    else R.string.cd_close
                )
            )
        }

        if (uiState.isLoading) {
            LoadingWheel()
        } else if (uiState.error != null) {
            ErrorDialog(message = uiState.error) {
                onAction(DogDetailUiAction.DismissError)
            }
        }

        if (probableDogsDialogEnabled.value) {
            MostProbableDogsDialog(
                mostProbableDogs = uiState.probableDogs,
                onShowMostProbableDogsDialogDismiss = {
                    probableDogsDialogEnabled.value = false
                },
                onItemClicked = {
                    onAction(DogDetailUiAction.UpdateDog(it))
                }
            )
        }
    }
}

@Composable
fun DogInformation(
    dog: Dog,
    isRecognition: Boolean,
    topPadding: Dp,
    onProbableDogsButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.dog_index_format, dog.index),
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                    text = dog.name,
                    fontSize = 32.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                LifeIcon()

                Text(
                    text = stringResource(
                        id = R.string.dog_life_expectancy_format,
                        dog.lifeExpectancy
                    ),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = dog.temperament,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (isRecognition) {
                    Button(
                        modifier = Modifier.padding(16.dp),
                        onClick = {
                            onProbableDogsButtonClick()
                        }) {
                        Text(
                            text = stringResource(id = R.string.not_your_dog),
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(
                        top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp
                    ),
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DogDataColumn(
                        modifier = Modifier.weight(1f),
                        stringResource(id = R.string.female),
                        dog.weightFemale,
                        dog.heightFemale
                    )

                    VerticalDivider()

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dog.type,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.group),
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    VerticalDivider()

                    DogDataColumn(
                        modifier = Modifier.weight(1f),
                        stringResource(id = R.string.male),
                        dog.weightMale,
                        dog.heightMale
                    )
                }
            }
        }
    }
}

@Composable
private fun LifeIcon() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 80.dp, end = 80.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_hearth_white),
                tint = Color.Unspecified,
                contentDescription = null,
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
                    .padding(4.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(bottomEnd = 2.dp, topEnd = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.error
        ) {

        }
    }
}

@Composable
private fun DogDataColumn(
    modifier: Modifier = Modifier,
    genre: String,
    weight: String,
    height: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = genre,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = weight,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = stringResource(id = R.string.weight),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = height,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = stringResource(id = R.string.height),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)
@PreviewLightDark
@PreviewScreenSizes
@Composable
fun DogDetailScreenPreview() {
    val windowSizeClass =
        WindowSizeClass.calculateFromSize(
            androidx.compose.ui.unit.DpSize(400.dp, 800.dp)
        )
    DogedexTheme {
        val dog = Dog(
            1L, 78, "Pug",
            "Herding", "70", "75",
            "", "10 - 12", "Friendly, playful",
            "5", "6"
        )
        DogDetailContent(
            windowSizeClass = windowSizeClass,
            uiState = DogDetailUiState(dog = dog),
            dog = dog,
            onAction = {}
        )
    }
}
