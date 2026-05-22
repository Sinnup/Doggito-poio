package com.fruse.dogedex.core.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun BackNavigationIcon(tint: Color = Color.Black, onClick: () -> Unit,) {
    IconButton(onClick = onClick) {
        Icon(
            painter = rememberVectorPainter(image = Icons.AutoMirrored.Sharp.ArrowBack),
            contentDescription = null,
            tint = tint
        )
    }
}
