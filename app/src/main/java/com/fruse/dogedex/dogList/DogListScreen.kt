package com.fruse.dogedex.dogList

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.fruse.dogedex.R
import com.fruse.dogedex.core.composables.BackNavigationIcon
import com.fruse.dogedex.core.composables.ErrorDialog
import com.fruse.dogedex.core.composables.LoadingWheel
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.core.ui.theme.DogedexTheme


private const val GRID_SPAN_COUNT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogListScreen(
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
        uiState = uiState,
        onNavigationIconClick = onNavigateBack,
        onDogClicked = { viewModel.handleAction(DogListUiAction.OnDogClicked(it)) },
        onDismissError = { viewModel.handleAction(DogListUiAction.DismissError) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DogListContent(
    uiState: DogListUiState,
    onNavigationIconClick: () -> Unit,
    onDogClicked: (Dog) -> Unit,
    onDismissError: () -> Unit
) {
    Scaffold(
        topBar = { DogListScreenTopBar { onNavigationIconClick() } },
    ) { paddingValues ->
        LazyVerticalGrid(
            contentPadding = paddingValues,
            columns = GridCells.Fixed(GRID_SPAN_COUNT),
            content = {
                items(uiState.dogs) { dog ->
                    DogGridItem(dog = dog, onDogClicked = onDogClicked)
                }
            })
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
    TopAppBar(
        title = { Text(text = stringResource(R.string.my_dog_collection)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = { BackNavigationIcon(onClick) }
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DogGridItem(dog: Dog, onDogClicked: (Dog) -> Unit) {
    if (dog.inCollection) {
        val filesDir = LocalContext.current.filesDir
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .height(100.dp)
                .width(100.dp),
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
                    .semantics { testTag = "dog-${dog.name}" }
            )
        }
    } else {
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .height(100.dp)
                .width(100.dp),
            color = Color.Red,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                text = dog.index.toString(),
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

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
    DogedexTheme {
        DogListContent(
            uiState = DogListUiState(),
            onNavigationIconClick = {},
            onDogClicked = {},
            onDismissError = {}
        )
    }
}
