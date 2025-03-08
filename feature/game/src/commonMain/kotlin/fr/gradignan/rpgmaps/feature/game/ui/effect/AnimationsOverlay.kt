package fr.gradignan.rpgmaps.feature.game.ui.effect

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.feature.game.PING_RADIUS_PX
import fr.gradignan.rpgmaps.feature.game.ui.map.CoordinatesConverter


@Composable
internal fun AnimationsOverlay(
    pings: List<MapEffect.Ping>,
    transformer: CoordinatesConverter,
    modifier: Modifier = Modifier
) {
    if (pings.isNotEmpty()) {
        val infiniteTransition = rememberInfiniteTransition()
        val radius by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = PING_RADIUS_PX,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
        val pingColor = MaterialTheme.colorScheme.error.copy(alpha = 1 - radius / PING_RADIUS_PX)
        Canvas(modifier = modifier) {
            drawPings(pings, transformer, radius, pingColor)
        }
    }
}
