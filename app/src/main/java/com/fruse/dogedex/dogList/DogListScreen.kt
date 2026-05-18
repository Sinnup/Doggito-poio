package com.fruse.dogedex.dogList

import android.annotation.SuppressLint
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
import coil.compose.rememberAsyncImagePainter
import com.fruse.dogedex.R
import com.fruse.dogedex.api.responses.ApiResponseStatus
import com.fruse.dogedex.core.composables.BackNavigationIcon
import com.fruse.dogedex.core.composables.ErrorDialog
import com.fruse.dogedex.core.composables.LoadingWheel
import com.fruse.dogedex.core.model.Dog
import com.fruse.dogedex.core.ui.theme.DogedexTheme
import kotlinx.coroutines.flow.Flow


private const val GRID_SPAN_COUNT = 3

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DogListScreen(
    onNavigationIconClick: () -> Unit,
    onDogClicked: (Dog) -> Unit,
    viewModel: DogListViewModel = hiltViewModel()
) {
    val dogList = viewModel.dogList.value
    val status = viewModel.status.value

    Scaffold(
        topBar = { DogListScreenTopBar { onNavigationIconClick() } },
    ) {
        LazyVerticalGrid(
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            columns = GridCells.Fixed(GRID_SPAN_COUNT),
            content = {
                items(dogList) {
                    DogGridItem(dog = it, onDogClicked = onDogClicked)
                }
            })
    }

    if (status is ApiResponseStatus.Loading) {
        LoadingWheel()
    } else if (status is ApiResponseStatus.Error) {
        ErrorDialog(messageId = status.messageId) {
            viewModel.resetApiResponseStatus()
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
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .height(100.dp)
                .width(100.dp),
            onClick = { onDogClicked(dog) },
            shape = RoundedCornerShape(4.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = dog.imageUrl),
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
        DogListScreen(
            onNavigationIconClick = { /*TODO*/ },
            onDogClicked = { _ -> },
            viewModel = DogListViewModel(
                dogRepository = object : DogTasks {
                    override suspend fun getDogCollection(): ApiResponseStatus<List<Dog>> {
                        TODO("Not yet implemented")
                    }

                    override suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any> {
                        TODO("Not yet implemented")
                    }

                    override suspend fun getDogBYMlId(mlDogId: String): ApiResponseStatus<Dog> {
                        TODO("Not yet implemented")
                    }

                    override suspend fun getProbableDogs(probableDogsIds: List<String>): Flow<ApiResponseStatus<Dog>> {
                        TODO("Not yet implemented")
                    }

                }
            ))
    }
}