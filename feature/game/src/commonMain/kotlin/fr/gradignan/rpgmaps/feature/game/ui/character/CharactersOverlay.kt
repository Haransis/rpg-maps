package fr.gradignan.rpgmaps.feature.game.ui.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import fr.gradignan.rpgmaps.core.common.format
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.feature.game.CHARACTER_RADIUS
import fr.gradignan.rpgmaps.feature.game.SELECTED_CHARACTER_RADIUS
import fr.gradignan.rpgmaps.feature.game.model.DistancePath
import fr.gradignan.rpgmaps.feature.game.model.GameIntent
import fr.gradignan.rpgmaps.feature.game.ui.effect.drawLaser
import fr.gradignan.rpgmaps.feature.game.ui.map.CoordinatesConverter
import fr.gradignan.rpgmaps.feature.game.ui.map.handleMapGestures
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


@Composable
internal fun CharactersOverlay(
    laserPosition: Offset?,
    ruler: DistancePath,
    previewPath: DistancePath,
    mapCharacters: List<MapCharacter>,
    transformer: CoordinatesConverter,
    hoveredCharacterId: Int?,
    selectedMapCharacter: MapCharacter?,
    action: (GameIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                with(transformer) {
                    detectTapGestures (
                        onDoubleTap = {
                            action(GameIntent.DoubleClick)
                        },
                        onTap = {
                            action(GameIntent.MapClick(toAbsoluteOffset(it)))
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                with(transformer) {
                    handleMapGestures(
                        onUnselect = { action(GameIntent.Unselect) },
                        onPointerMove = {
                            val newPosition = toAbsoluteOffset(it)
                            action(GameIntent.PointerMove(newPosition))
                        }
                    )
                }
            }
    ) {
        drawMovementIndicator(
            previewPath = ruler,
            transformer = transformer,
            textMeasurer = textMeasurer
        )
        laserPosition?.let {
            drawLaser(it,transformer)
        }
        selectedMapCharacter?.let {
            drawMovementIndicator(
                previewPath = previewPath,
                transformer = transformer,
                textMeasurer = textMeasurer,
                color = Color.Green
            )
        }
        drawCharacters(
            mapCharacters = mapCharacters,
            selectedMapCharacter = selectedMapCharacter,
            hoveredCharacterId = hoveredCharacterId,
            coordinatesConverter = transformer,
            textMeasurer = textMeasurer
        )
    }
}
