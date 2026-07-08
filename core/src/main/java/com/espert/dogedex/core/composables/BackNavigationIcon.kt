package com.espert.dogedex.core.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.espert.dogedex.core.R

@Composable
fun BackNavigationIcon(tint: Color = Color.Black, onClick: () -> Unit,) {
    IconButton(onClick = onClick) {
        Icon(
            painter = rememberVectorPainter(image = Icons.AutoMirrored.Sharp.ArrowBack),
            contentDescription = stringResource(R.string.cd_navigate_back),
            tint = tint
        )
    }
}
