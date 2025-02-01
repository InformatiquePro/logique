package fr.charleselie.logique.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp

val buttonHoverAnimation = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

@ExperimentalAnimationApi
fun enterTransition() = slideInVertically(
    initialOffsetY = { 300 },
    animationSpec = tween(durationMillis = 300)
) + fadeIn(animationSpec = tween(durationMillis = 300))

@ExperimentalAnimationApi
fun exitTransition() = slideOutVertically(
    targetOffsetY = { -300 },
    animationSpec = tween(durationMillis = 300)
) + fadeOut(animationSpec = tween(durationMillis = 300)) 