package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.lopezapp.movilpos.ui.model.AnimationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val animationDurationMs: Int = 400,
    val animationType: AnimationType = AnimationType.SLIDE_AND_FADE
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateAnimationDuration(durationMs: Int) {
        _uiState.update { it.copy(animationDurationMs = durationMs.coerceIn(100, 1000)) }
    }

    fun updateAnimationType(type: AnimationType) {
        _uiState.update { it.copy(animationType = type) }
    }
}
