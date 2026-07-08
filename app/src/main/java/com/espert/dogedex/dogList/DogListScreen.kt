package com.espert.dogedex.dogList

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.espert.dogedex.R
import com.espert.dogedex.core.composables.BackNavigationIcon
import com.espert.dogedex.core.composables.ErrorDialog
import com.espert.dogedex.core.composables.LoadingWheel
import com.espert.dogedex.core.model.Dog
import com.espert.dogedex.core.ui.theme.DogedexTheme
import com.espert.dogedex.ui.isExpanded
import com.espert.dogedex.ui.isMedium

private fun columnCountFor(windowSizeClass: WindowSizeClass): Int = when {
    windowSizeClass.isExpanded -> 5
    windowSizeClass.isMedium   -> 4
    else                       -> 3
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogListScreen(
    windowSizeClass: WindowSizeClass,
    onNavigateToDogDetail: (Dog) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DogListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DogListUiEffect.NavigateToDogDetail -> onNavigateToDogDetail(effect.dog)
            }
        }
    }

    DogListContent(
        windowSizeClass = windowSizeClass,
        uiState = uiState,
        onNavigationIconClick = onNavigateBack,
        onDogClicked = { viewModel.handleAction(DogListUiAction.OnDogClicked(it)) },
        onDismissError = { viewModel.handleAction(DogListUiAction.DismissError) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DogListContent(
    windowSizeClass: WindowSizeClass,
    uiState: DogListUiState,
    onNavigationIconClick: () -> Unit,
    onDogClicked: (Dog) -> Unit,
    onDismissError: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { DogListScreenTopBar { onNavigationIconClick() } },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val noneCollected = uiState.dogs.isNotEmpty() && uiState.dogs.none { it.inCollection }
            LazyVerticalGrid(
                contentPadding = paddingValues,
                columns = GridCells.Fixed(columnCountFor(windowSizeClass)),
                modifier = Modifier.widthIn(max = 960.dp),
                content = {
                    if (noneCollected) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyCollectionMessage()
                        }
                    }
                    items(uiState.dogs) { dog ->
                        DogGridItem(dog = dog, onDogClicked = onDogClicked)
                    }
                })
        }
    }

    if (uiState.isLoading) {
        LoadingWheel()
    } else if (uiState.error != null) {
        ErrorDialog(message = uiState.error) {
            onDismissError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogListScreenTopBar(onClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(
            fontFamily = FontFamily.Serif, text = stringResource(R.string.my_dog_collection)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = { BackNavigationIcon(onClick = onClick) }
    )
}

@Composable
private fun EmptyCollectionMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_baseline_list),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = stringResource(R.string.dog_list_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.dog_list_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DogGridItem(dog: Dog, onDogClicked: (Dog) -> Unit) {
    if (dog.inCollection) {
        val filesDir = LocalContext.current.filesDir
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
            onClick = { onDogClicked(dog) },
            shape = RoundedCornerShape(4.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = java.io.File(filesDir, "images/${dog.imageUrl}.jpg")
                ),
                contentDescription = null,
                modifier = Modifier
                    .background(Color.White)
                    .semantics { testTag = "dog-${dog.name}" },
                contentScale = ContentScale.Crop
            )
        }
    } else {
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .aspectRatio(1f),
            color = MaterialTheme.colorScheme.inversePrimary,
            shape = RoundedCornerShape(4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = dog.index.toString(),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    textAlign = TextAlign.Center,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight"
)
@Composable
fun DogListScreenPreview() {
    val windowSizeClass = androidx.compose.material3.windowsizeclass.WindowSizeClass.calculateFromSize(
        androidx.compose.ui.unit.DpSize(400.dp, 800.dp)
    )
    DogedexTheme {
        DogListContent(
            windowSizeClass = windowSizeClass,
            uiState = DogListUiState(),
            onNavigationIconClick = {},
            onDogClicked = {},
            onDismissError = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun DogGridItemPreview() {
    DogedexTheme {
        DogGridItem(
            dog = Dog(
                id = 123L,
                index = 123,
                name = "asdasd",
                type = "asdasd",
                heightFemale = "asdasd",
                heightMale = "asdasd",
                imageUrl = "12312312332",
                lifeExpectancy = "12",
                temperament = "35",
                weightFemale = "weightFemale",
                weightMale = "weightMale",
                inCollection = false
            ), {})
    }
}