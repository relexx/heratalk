// Copyright (c) 2026 relexx. BSD 3-Clause License.
// See LICENSE file in the project root for full license information.
package de.relexx.heratalk.feature.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.relexx.heratalk.core.identity.IdentityRepository
import de.relexx.heratalk.core.model.DisplayName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Maximum allowed length of a [DisplayName] in Unicode code points (architecture.md §6.1).
 */
public const val DISPLAY_NAME_MAX_CODEPOINTS: Int = 32

/**
 * View-model backing the pairing flow.
 *
 * Owns the editable [DisplayName] input state and persists a valid name through
 * [IdentityRepository]. The view-model deliberately holds only UI state — the
 * DataStore round-trip lives in `:core:identity`.
 *
 * **Validation contract:**
 * - Trim is *not* applied; the user keeps trailing whitespace if they typed it,
 *   but [DisplayName.value] enforces "at least one visible character" downstream.
 * - The codepoint count drives the live counter and the "too long" gate at 32.
 * - The "Continue" button is enabled iff [DisplayNameInputState.canSubmit] is `true`.
 *
 * The view-model is unaware of navigation; the screen reads
 * [DisplayNameInputState.persistResult] to decide whether to advance.
 *
 * @param identityRepository Repository that persists the display name on
 *   successful submission. Injected by Koin in v0.1.0/D1.
 */
public class PairingViewModel(
    private val identityRepository: IdentityRepository,
) : ViewModel() {
    private val internalState: MutableStateFlow<DisplayNameInputState> =
        MutableStateFlow(DisplayNameInputState.Empty)

    /** Hot, conflated state for the display-name input screen. */
    public val displayNameState: StateFlow<DisplayNameInputState> = internalState.asStateFlow()

    /**
     * Updates the display-name draft.
     *
     * Re-evaluates validation and submit-eligibility on every keystroke.
     */
    public fun onDisplayNameChanged(draft: String) {
        val codepoints = draft.codePointCount(0, draft.length)
        val validationError =
            when {
                draft.isEmpty() || draft.isBlank() -> DisplayNameValidationError.Empty
                codepoints > DISPLAY_NAME_MAX_CODEPOINTS -> DisplayNameValidationError.TooLong
                else -> null
            }
        internalState.update {
            DisplayNameInputState(
                draft = draft,
                codepointCount = codepoints,
                validationError = validationError,
                persistResult = PersistResult.Idle,
            )
        }
    }

    /**
     * Persists the current draft if it is valid.
     *
     * On success the [DisplayNameInputState.persistResult] transitions to
     * [PersistResult.Saved] so the screen can navigate forward. On failure
     * (e.g. [IllegalArgumentException] from [DisplayName.init] guarding against
     * Bidi-overrides) it transitions to [PersistResult.Error] and the screen
     * surfaces the error message via the existing [DisplayNameValidationError]
     * channel.
     */
    public fun onSubmit() {
        val current = internalState.value
        if (!current.canSubmit) return
        viewModelScope.launch {
            val name =
                runCatching { DisplayName(current.draft) }
                    .getOrElse { error ->
                        internalState.update {
                            it.copy(persistResult = PersistResult.Error(error.message ?: "invalid name"))
                        }
                        return@launch
                    }
            identityRepository.setDisplayName(name)
            internalState.update { it.copy(persistResult = PersistResult.Saved) }
        }
    }
}

/**
 * Snapshot of the display-name input state.
 *
 * @property draft Raw user input, never normalised on its way through the view-model.
 * @property codepointCount Pre-computed Unicode code-point length for the live counter.
 * @property validationError `null` when [draft] passes lightweight validation, otherwise
 *   the specific reason for the user-facing error.
 * @property persistResult Outcome of the most recent submission attempt.
 */
public data class DisplayNameInputState(
    public val draft: String,
    public val codepointCount: Int,
    public val validationError: DisplayNameValidationError?,
    public val persistResult: PersistResult,
) {
    /** `true` iff the current [draft] is allowed to be persisted. */
    public val canSubmit: Boolean
        get() = validationError == null && draft.isNotEmpty()

    public companion object {
        /** Initial empty state used by the view-model. */
        public val Empty: DisplayNameInputState =
            DisplayNameInputState(
                draft = "",
                codepointCount = 0,
                validationError = DisplayNameValidationError.Empty,
                persistResult = PersistResult.Idle,
            )
    }
}

/**
 * Reasons why a display-name draft cannot be submitted.
 */
public enum class DisplayNameValidationError {
    /** Empty or pure-whitespace input. */
    Empty,

    /** Input exceeds [DISPLAY_NAME_MAX_CODEPOINTS] code points. */
    TooLong,
}

/**
 * Outcome of the most recent persistence attempt.
 */
public sealed interface PersistResult {
    /** No submit attempt has been made for the current draft. */
    public data object Idle : PersistResult

    /** The draft was successfully written to [IdentityRepository]. */
    public data object Saved : PersistResult

    /**
     * The draft failed validation inside [DisplayName] (e.g. Bidi-override).
     *
     * @property reason Human-readable diagnostic; for logging only — the screen
     *   shows a localised error message instead of this string verbatim.
     */
    public data class Error(
        public val reason: String,
    ) : PersistResult
}
