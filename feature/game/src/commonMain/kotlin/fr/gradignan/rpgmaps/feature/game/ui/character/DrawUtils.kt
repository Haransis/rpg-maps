package fr.gradignan.rpgmaps.feature.game.ui.character

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import fr.gradignan.rpgmaps.core.common.format
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.feature.game.CHARACTER_RADIUS
import fr.gradignan.rpgmaps.feature.game.SELECTED_CHARACTER_RADIUS
import fr.gradignan.rpgmaps.feature.game.model.DistancePath
import fr.gradignan.rpgmaps.feature.game.ui.map.CoordinatesConverter
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


internal fun DrawScope.drawUnReachableMovement(start: Offset, end: Offset) {
    drawLine(
        color = Color.Gray,
        start = start,
        end = end,
        strokeWidth = 2f
    )
}

internal fun DrawScope.drawReachableMovement(start: Offset, end: Offset, color: Color, strokeWidth: Float = 2f) {
    val arrowSize = 20f
    val insetFactor = 0.6f
    val angle = atan2(end.y - start.y, end.x - start.x)
    val arrowLeft = Offset(
        end.x - arrowSize * cos(angle - PI.toFloat() / 6),
        end.y - arrowSize * sin(angle - PI.toFloat() / 6)
    )
    val arrowRight = Offset(
        end.x - arrowSize * cos(angle + PI.toFloat() / 6),
        end.y - arrowSize * sin(angle + PI.toFloat() / 6)
    )
    val adjustedEnd = Offset(
        end.x - (strokeWidth + 5f / 2f) * cos(angle),
        end.y - (strokeWidth + 5f / 2f) * sin(angle)
    )
    val mid = (arrowLeft + arrowRight) / 2f
    val arrowMid = Offset(
        end.x + insetFactor * (mid.x - end.x),
        end.y + insetFactor * (mid.y - end.y)
    )

    drawLine(
        color = color,
        start = start,
        end = adjustedEnd,
        strokeWidth = strokeWidth
    )

    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(arrowLeft.x, arrowLeft.y)
        lineTo(arrowMid.x, arrowMid.y)
        lineTo(arrowRight.x, arrowRight.y)
        close()
    }
    drawPath(path, color = color)
}

internal fun DrawScope.drawMovementIndicator(
    previewPath: DistancePath,
    textMeasurer: TextMeasurer,
    transformer: CoordinatesConverter,
    color: Color = Color.Black
) = with(transformer) {
    if (previewPath.reachable.size <= 1) return@with
    previewPath.reachable.windowed(2).forEach { (startRaw, endRaw) ->
        val start = toMapOffset(startRaw)
        val end = toMapOffset(endRaw)
        drawReachableMovement(start, end, color)
    }
    previewPath.unreachableStop?.let {
        val start = toMapOffset(previewPath.reachable.last())
        val end = toMapOffset(it)
        drawUnReachableMovement(start, end)
    }

    val style = TextStyle(
        fontSize = 20.sp,
        shadow = Shadow(color = Color.White, blurRadius = 1f)
    )
    val textLayout = textMeasurer.measure("${previewPath.totalDistance.format(1)}m", style)
    drawText(
        textLayoutResult = textLayout,
        topLeft = toMapOffset(previewPath.unreachableStop ?: previewPath.reachable.last())+ Offset(10f, 0f),
    )
}

internal fun DrawScope.drawCharacters(
    mapCharacters: List<MapCharacter>,
    selectedMapCharacter: MapCharacter?,
    hoveredCharacterId: Int?,
    coordinatesConverter: CoordinatesConverter,
    textMeasurer: TextMeasurer
) {
    with(coordinatesConverter) {
        mapCharacters.forEach { character ->
            val center = toMapOffset(character.x, character.y)
            val radius = toMapOffset(if (character == selectedMapCharacter) SELECTED_CHARACTER_RADIUS else CHARACTER_RADIUS)
            drawCircle(
                color = character.color.toColor(),
                radius = radius,
                center = center,
            )
            drawCircle(
                color = Color.Black,
                radius = radius,
                center = center,
                style = Stroke(width = 1f)
            )
            if (hoveredCharacterId == character.cmId) {
                val style = TextStyle(
                    fontSize = 20.sp,
                    shadow = Shadow(color = Color.White, blurRadius = 1f)
                )
                val outlineTextResult = textMeasurer.measure(character.name, style)
                drawText(
                    textLayoutResult = outlineTextResult,
                    topLeft = Offset(
                        x = center.x + radius*1.1f - outlineTextResult.size.width / 2,
                        y = center.y + radius*1.1f - outlineTextResult.size.height / 2,
                    )
                )
            }
        }
    }
}

private fun String.toColor(): Color = when(this) {
    "green" -> Color.Green
    else -> Color.Gray
}
