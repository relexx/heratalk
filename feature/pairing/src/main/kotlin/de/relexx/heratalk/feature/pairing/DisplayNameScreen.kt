// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.feature.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.relexx.heratalk.core.model.NetworkQuality
import de.relexx.heratalk.core.ui.components.HeraTalkScaffold
import de.relexx.heratalk.core.ui.theme.HeraTalkTheme

/**
 * Pairing-flow display-name input screen.
 *
 * Stateful wrapper that observes a [PairingViewModel] and renders the input,
 * validation and submission UX. The screen is stateless under the hood — all
 * state lives in the view-model — so it is safe to reuse from the settings
 * "Your name" entry once navigation is wired up in Phase E.
 *
 * **Validation UX:**
 * - Live code-point counter under the input field.
 * - Submit button disabled until [DisplayNameInputState.canSubmit] flips to `true`.
 * - Localised error text under the field on validation failures.
 *
 * @param state Current view-model state.
 * @param onDraftChange Forwarded to [PairingViewModel.onDisplayNameChanged].
 * @param onSubmit Forwarded to [PairingViewModel.onSubmit].
 * @param onSaved Invoked exactly once when the persistence transitions to
 *   [PersistResult.Saved]. Use this for navigation.
 * @param modifier Optional layout modifier.
 */
@Composable
public fun DisplayNameScreen(
    state: DisplayNameInputState,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.persistResult) {
        if (state.persistResult is PersistResult.Saved) {
            onSaved()
        }
    }

    HeraTalkScaffold(
        title = stringResource(R.string.pairing_display_name_title),
        networkQuality = NetworkQuality.OFFLINE,
        modifier = modifier,
    ) { innerPadding ->
        DisplayNameContent(
            innerPadding = innerPadding,
            state = state,
            onDraftChange = onDraftChange,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun DisplayNameContent(
    innerPadding: PaddingValues,
    state: DisplayNameInputState,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.pairing_display_name_hint),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = state.draft,
            onValueChange = onDraftChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.pairing_display_name_placeholder)) },
            singleLine = true,
            isError = state.validationError != null && state.draft.isNotEmpty(),
            supportingText = {
                ValidationSupportingText(state = state)
            },
        )
        Text(
            text = stringResource(R.string.pairing_display_name_counter, state.codepointCount),
            style = MaterialTheme.typography.labelSmall,
        )
        Button(
            onClick = onSubmit,
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.pairing_display_name_continue))
        }
    }
}

@Composable
private fun ValidationSupportingText(state: DisplayNameInputState) {
    // Empty-state error is suppressed while the field is empty so we don't
    // shout at the user before they have typed anything.
    val errorRes =
        when (state.validationError) {
            DisplayNameValidationError.Empty ->
                if (state.draft.isEmpty()) null else R.string.pairing_display_name_error_empty
            DisplayNameValidationError.TooLong -> R.string.pairing_display_name_error_too_long
            null -> null
        }
    if (errorRes != null) {
        Text(text = stringResource(errorRes))
    }
}

@Preview(name = "Light · empty", showBackground = true)
@Composable
private fun DisplayNameScreenPreviewLightEmpty() {
    HeraTalkTheme(darkTheme = false) {
        var state by remember { mutableStateOf(DisplayNameInputState.Empty) }
        DisplayNameScreen(
            state = state,
            onDraftChange = { draft ->
                state =
                    state.copy(
                        draft = draft,
                        codepointCount = draft.codePointCount(0, draft.length),
                        validationError =
                            when {
                                draft.isBlank() -> DisplayNameValidationError.Empty
                                draft.codePointCount(0, draft.length) > DISPLAY_NAME_MAX_CODEPOINTS ->
                                    DisplayNameValidationError.TooLong
                                else -> null
                            },
                    )
            },
            onSubmit = {},
            onSaved = {},
        )
    }
}

@Preview(name = "Dark · filled", showBackground = true)
@Composable
private fun DisplayNameScreenPreviewDarkFilled() {
    // Preview literals are not user-visible at runtime — no i18n required.
    @Suppress("HardcodedText")
    val filled =
        DisplayNameInputState(
            draft = "Pascal",
            codepointCount = 6,
            validationError = null,
            persistResult = PersistResult.Idle,
        )
    HeraTalkTheme(darkTheme = true) {
        DisplayNameScreen(
            state = filled,
            onDraftChange = {},
            onSubmit = {},
            onSaved = {},
        )
    }
}
