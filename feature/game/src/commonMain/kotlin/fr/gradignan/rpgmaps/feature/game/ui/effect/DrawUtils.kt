package fr.gradignan.rpgmaps.feature.game.ui.effect

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.feature.game.ui.map.CoordinatesConverter


internal fun DrawScope.drawLaser(
    position: Offset,
    transformer: CoordinatesConverter
) = with(transformer) {
    drawCircle(
        color = Color.Red,
        radius = 6f,
        center = toMapOffset(position)
    )
}

internal fun DrawScope.drawPings(
    pings: List<MapEffect.Ping>,
    coordinatesConverter: CoordinatesConverter,
    radius: Float,
    pingColor: Color
) = with(coordinatesConverter) {
    pings.forEach { ping ->
        drawCircle(
            color = pingColor,
            radius = radius,
            center = toMapOffset(ping.x, ping.y)
        )
    }
}
