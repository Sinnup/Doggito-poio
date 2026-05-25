package com.espert.dogedex.core.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.espert.dogedex.core.R
import com.espert.dogedex.core.ui.theme.DogedexTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthField(
    label: String,
    email: String,
    ontTextChanged: (String) -> Unit,
    modifier: Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    errorMessageId: Int? = null,
    errorMessage: String? = null,
    errorSemantic: String = "",
    fieldSemantic: String = ""
) {
    val resolvedError = when {
        errorMessage != null -> errorMessage
        errorMessageId != null -> stringResource(id = errorMessageId)
        else -> null
    }
    Surface {
        Column(modifier = modifier) {
            if (resolvedError != null) {
                Text(
                    text = resolvedError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = errorSemantic },
                    color = MaterialTheme.colorScheme.error
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = { ontTextChanged(it) },
                label = { Text(text = label) },
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = fieldSemantic },
                isError = resolvedError != null,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    unfocusedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,
                )
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
fun PreviewAuthField() {
    DogedexTheme {
        AuthField(
            label = "Test",
            email = "Test",
            ontTextChanged = {},
            modifier = Modifier,
            errorMessageId = R.string.passwords_do_not_match
        )
    }
}