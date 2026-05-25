package com.espert.dogedex.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.espert.dogedex.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { viewModel.logout() }) {
            Text(text = stringResource(id = R.string.logout))
        }
    }
}
