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
import com.lopezapp.movilpos.ui.model.AnimationType

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
