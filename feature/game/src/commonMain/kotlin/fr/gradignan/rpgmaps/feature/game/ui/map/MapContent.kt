package fr.gradignan.rpgmaps.feature.game.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.game.model.GameIntent
import fr.gradignan.rpgmaps.feature.game.model.MapState
import fr.gradignan.rpgmaps.feature.game.ui.character.CharactersOverlay
import fr.gradignan.rpgmaps.feature.game.ui.effect.AnimationsOverlay


@Composable
internal fun MapContent(
    mapState: MapState,
    painter: AsyncImagePainter,
    painterState: AsyncImagePainter.State.Success,
    transformState: TransformState,
    action: (GameIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .pointerHoverIcon(
                    when {
                        mapState.hoveredCharacterId != null -> PointerIcon.Hand
                        mapState.isRulerChecked -> PointerIcon.Crosshair
                        else -> PointerIcon.Default
                    },
                    overrideDescendants = true
                )
                .graphicsLayer {
                    scaleX = transformState.scale
                    scaleY = transformState.scale
                    translationX = transformState.offset.x
                    translationY = transformState.offset.y
                }
                .detectDragMap { dragAmount -> transformState.applyPan(dragAmount) }
                .detectZoom { delta -> transformState.applyZoom(delta) }
        ) {
            val transformer: CoordinatesConverter = remember(painterState) {
                CoordinatesConverter(painterState.result.image.width, painterState.result.image.height)
            }
            Image(
                painter = painter,
                contentDescription = "Zoomable Image",
                contentScale = ContentScale.Inside,
                modifier = Modifier.fillMaxSize()
            )
            CharactersOverlay(
                laserPosition = mapState.laserPosition,
                ruler = mapState.ruler,
                previewPath = mapState.previewPath,
                mapCharacters = mapState.mapCharacters,
                transformer = transformer,
                hoveredCharacterId = mapState.hoveredCharacterId,
                selectedMapCharacter = mapState.selectedMapCharacter,
                action = action,
                modifier = Modifier.matchParentSize()
                    .pointerHoverIcon(if (mapState.hoveredCharacterId != null) PointerIcon.Hand else PointerIcon.Default)
            )
            AnimationsOverlay(
                pings = mapState.pings,
                transformer = transformer,
                modifier = Modifier.matchParentSize()
            )
        }
        SettingsControls(
            isAdmin = mapState.isAdmin,
            isGmChecked = mapState.isGmChecked,
            isSprintChecked = mapState.isSprintChecked,
            isPingChecked = mapState.isPingChecked,
            isRulerChecked = mapState.isRulerChecked,
            action = action,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        MapControls(
            transformState = transformState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        AnimatedVisibility(
            visible = mapState.error != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.TopStart)
                .padding(MaterialTheme.spacing.small)
        ) {
            mapState.error?.let { error ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "${error.asString()} ..."
                    )
                }
            }
        }
    }
}
