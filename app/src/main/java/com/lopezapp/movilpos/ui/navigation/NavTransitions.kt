package com.lopezapp.movilpos.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.lopezapp.movilpos.ui.model.AnimationType
import com.lopezapp.movilpos.ui.viewmodel.SettingsViewModel

fun buildNavTransition(
    animationType: AnimationType,
    durationMs: Int,
    isPop: Boolean = false
): ContentTransform {
    val duration = durationMs.coerceAtLeast(1)

    return when (animationType) {
        AnimationType.SLIDE_AND_FADE -> {
            val enterSlide = slideInHorizontally(
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> if (isPop) -fullWidth else fullWidth }
            )
            val exitSlide = slideOutHorizontally(
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> if (isPop) fullWidth else -fullWidth }
            )
            val enterFade = fadeIn(animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing))
            val exitFade = fadeOut(animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing))

            (enterSlide + enterFade) togetherWith (exitSlide + exitFade)
        }
        AnimationType.FADE_ONLY -> {
            val enterFade = fadeIn(animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing))
            val exitFade = fadeOut(animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing))

            enterFade togetherWith exitFade
        }
        AnimationType.SLIDE_ONLY -> {
            val enterSlide = slideInHorizontally(
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                initialOffsetX = { fullWidth -> if (isPop) -fullWidth else fullWidth }
            )
            val exitSlide = slideOutHorizontally(
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                targetOffsetX = { fullWidth -> if (isPop) fullWidth else -fullWidth }
            )

            enterSlide togetherWith exitSlide
        }
        AnimationType.SCALE_AND_FADE -> {
            val enterScale = scaleIn(
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                initialScale = 0.85f
            ) + fadeIn(animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing))

            val exitScale = scaleOut(
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                targetScale = 0.85f
            ) + fadeOut(animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing))

            enterScale togetherWith exitScale
        }
        AnimationType.NONE -> {
            EnterTransition.None togetherWith ExitTransition.None
        }
    }
}

/**
 * Standardized reusable wrapper around Navigation 3's [NavDisplay] that automatically
 * collects [SettingsViewModel] state and applies configured transitions globally across the app.
 */
@Composable
fun <T : Any> AppNavDisplay(
    backStack: List<T>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sceneStrategy: SceneStrategy<T> = SinglePaneSceneStrategy(),
    settingsViewModel: SettingsViewModel = viewModel(),
    entryProvider: (key: T) -> NavEntry<T>
) {
    val settingsState by settingsViewModel.uiState.collectAsState()

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        modifier = modifier,
        sceneStrategy = sceneStrategy,
        transitionSpec = {
            buildNavTransition(
                animationType = settingsState.animationType,
                durationMs = settingsState.animationDurationMs,
                isPop = false
            )
        },
        popTransitionSpec = {
            buildNavTransition(
                animationType = settingsState.animationType,
                durationMs = settingsState.animationDurationMs,
                isPop = true
            )
        },
        entryProvider = entryProvider
    )
}

/**
 * Alias for [AppNavDisplay] to support [GlobalAnimatedNavDisplay] naming.
 */
@Composable
fun <T : Any> GlobalAnimatedNavDisplay(
    backStack: List<T>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    sceneStrategy: SceneStrategy<T> = SinglePaneSceneStrategy(),
    settingsViewModel: SettingsViewModel = viewModel(),
    entryProvider: (key: T) -> NavEntry<T>
) {
    AppNavDisplay(
        backStack = backStack,
        onBack = onBack,
        modifier = modifier,
        sceneStrategy = sceneStrategy,
        settingsViewModel = settingsViewModel,
        entryProvider = entryProvider
    )
}


