package com.espert.dogedex.dogDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.espert.dogedex.R
import com.espert.dogedex.core.model.Dog
import com.espert.dogedex.core.ui.theme.DogedexTheme

@Composable
fun MostProbableDogsDialog(
    mostProbableDogs: List<Dog>,
    onShowMostProbableDogsDialogDismiss: () -> Unit,
    onItemClicked: (Dog) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onShowMostProbableDogsDialogDismiss()
        },
        title = {
            Text(
                text = stringResource(id = R.string.other_probable_dogs),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            MostProbableDogsList(
                dogs = mostProbableDogs,
            ) {
                onItemClicked(it)
                onShowMostProbableDogsDialogDismiss()
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onShowMostProbableDogsDialogDismiss()
                }
            ) {
                Text(text = stringResource(R.string.dismiss))
            }
        },
    )
}

@Composable
fun MostProbableDogsList(dogs: List<Dog>, onItemClicked: (Dog) -> Unit) {
    Box(
        Modifier.height(250.dp)
    ) {
        LazyColumn(
            content = {
                items(dogs) {
                    MostProbableDogItem(dog = it, onItemClicked)
                }
            }
        )
    }
}

@Composable
fun MostProbableDogItem(dog: Dog, onItemClicked: (Dog) -> Unit) {
    val filesDir = LocalContext.current.filesDir
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = true) { onItemClicked(dog) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            dog.name,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Image(
            painter = rememberAsyncImagePainter(
                model = java.io.File(filesDir, "images/${dog.imageUrl}.jpg")
            ),
            contentDescription = dog.name,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@PreviewLightDark
@Composable
fun Prevista(){
    DogedexTheme {
        MostProbableDogsDialog(
            mostProbableDogs = fakeDogs(),
            onShowMostProbableDogsDialogDismiss = {},
            onItemClicked = {}
        )
    }
}

fun fakeDogs(): List<Dog>{
    val dogList = mutableListOf<Dog>()
    dogList.add(
        Dog(
            1, 1, "Chihuahua", "Chihuahua", "Toy",
            "19", "Brave", "12-15",
            "2.0", "2.5", "12.0", inCollection = false,
        )
    )

    dogList.add(
        Dog(
            1, 1, "Chihuahua", "Chihuahua", "Toy",
            "19", "Brave", "12-15",
            "2.0", "2.5", "12.0", inCollection = false,
        )
    )

    dogList.add(
        Dog(
            1, 1, "Chihuahua", "Chihuahua", "Toy",
            "19", "Brave", "12-15",
            "2.0", "2.5", "12.0", inCollection = false,
        )
    )

    return dogList.toList()
}