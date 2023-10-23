package com.fruse.dogedex.core.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.fruse.dogedex.core.R
import com.fruse.dogedex.core.ui.theme.DogedexTheme

@Composable
fun AuthField(
    label: String,
    email: String,
    ontTextChanged: (String) -> Unit,
    modifier: Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    errorMessageId: Int? = null,
    errorSemantic: String = "",
    fieldSemantic: String = ""
) {
    Column(modifier = modifier) {
        if (errorMessageId != null) {
            Text(
                text = stringResource(
                    id = errorMessageId
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = errorSemantic },
                color = MaterialTheme.colors.error
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
            isError = errorMessageId != null,
            colors = TextFieldDefaults
                .outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.primary,
                    cursorColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.primary
                )
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO
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