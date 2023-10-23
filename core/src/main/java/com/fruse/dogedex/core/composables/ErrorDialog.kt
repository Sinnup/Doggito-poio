package com.fruse.dogedex.core.composables

import androidx.annotation.StringRes
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.fruse.dogedex.core.R

@Composable
fun ErrorDialog(
    @StringRes messageId: Int,
    onErrorDialogDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.semantics { testTag = "error-dialog" },
        onDismissRequest = { },
        title = {
            Text(stringResource(R.string.oops_something_happened))
        },
        text = {
            Text(text = stringResource(id = messageId))
        },
        confirmButton = {
            Button(onClick = { onErrorDialogDismiss() }) {
                Text(text = stringResource(R.string.try_again))
            }
        })
}