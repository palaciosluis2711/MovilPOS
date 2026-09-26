package com.lopezapp.movilpos

import com.lopezapp.movilpos.ui.model.AnimationType
import com.lopezapp.movilpos.ui.navigation.buildNavTransition
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SettingsViewModelTest {

    @Test
    fun defaultSettings_hasCorrectDefaults() {
        val viewModel = SettingsViewModel()
        val state = viewModel.uiState.value

        assertEquals(400, state.animationDurationMs)
        assertEquals(AnimationType.SLIDE_AND_FADE, state.animationType)
    }

    @Test
    fun updateAnimationDuration_updatesAndClampsValue() {
        val viewModel = SettingsViewModel()

        viewModel.updateAnimationDuration(600)
        assertEquals(600, viewModel.uiState.value.animationDurationMs)

        // Test lower bound clamping
        viewModel.updateAnimationDuration(50)
        assertEquals(100, viewModel.uiState.value.animationDurationMs)

        // Test upper bound clamping
        viewModel.updateAnimationDuration(1500)
        assertEquals(1000, viewModel.uiState.value.animationDurationMs)
    }

    @Test
    fun updateAnimationType_updatesValueCorrectly() {
        val viewModel = SettingsViewModel()

        AnimationType.entries.forEach { type ->
            viewModel.updateAnimationType(type)
            assertEquals(type, viewModel.uiState.value.animationType)
        }
    }

    @Test
    fun buildNavTransition_returnsValidContentTransformForAllTypes() {
        AnimationType.entries.forEach { type ->
            val pushTransition = buildNavTransition(type, durationMs = 400, isPop = false)
            assertNotNull(pushTransition)

            val popTransition = buildNavTransition(type, durationMs = 400, isPop = true)
            assertNotNull(popTransition)
        }
    }
}
