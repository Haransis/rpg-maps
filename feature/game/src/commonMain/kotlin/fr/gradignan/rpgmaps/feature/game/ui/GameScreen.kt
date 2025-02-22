package fr.gradignan.rpgmaps.feature.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.gradignan.rpgmaps.core.common.roundToDecimals
import fr.gradignan.rpgmaps.core.model.Character
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.ui.error.UiText
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.game.model.HUDState
import fr.gradignan.rpgmaps.feature.game.model.MapEffectsState
import fr.gradignan.rpgmaps.feature.game.model.MapOverlayState
import org.jetbrains.compose.resources.imageResource
import org.koin.compose.viewmodel.koinViewModel
import rpg_maps.feature.game.generated.resources.Res
import rpg_maps.feature.game.generated.resources.defile1
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val PING_RADIUS_PX = 20f
private const val SELECTED_CHARACTER_RADIUS = 25
const val CHARACTER_RADIUS = 20

@Composable
fun GameScreenRoute(
    username: String,
    roomId: Int,
    admin: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = koinViewModel<GameViewModel>(),
) {
    LaunchedEffect(Unit) {
        viewModel.setName(username)
        viewModel.setRoomId(roomId)
        viewModel.setAdmin(admin)
    }
    GameScreen(
        onBack = onBack,
        viewModel = viewModel,
        modifier = modifier
    )
}

@Composable
fun GameScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = koinViewModel<GameViewModel>()
) {
    val effectsState: MapEffectsState by viewModel.effectsState.collectAsStateWithLifecycle()
    val overlayState: MapOverlayState by viewModel.overlayState.collectAsStateWithLifecycle()
    val hudState: HUDState by viewModel.hudState.collectAsStateWithLifecycle()

    when (val overlay = overlayState) {
        MapOverlayState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
        is MapOverlayState.UiStateMap -> {
            GameContent (
                onBack = onBack,
                onEndTurn = viewModel::onEndTurn,
                onSprintChecked = viewModel::onSprintChecked,
                onMapClick = viewModel::onMapClick,
                onUnselect = viewModel::onUnselect,
                effectsState = effectsState,
                hudState = hudState,
                overlayState = overlay,
                modifier = modifier
            )
        }
    }
}

@Composable
fun GameContent(
    onBack: () -> Unit,
    onEndTurn: () -> Unit,
    onSprintChecked: (Boolean) -> Unit,
    onMapClick: (Offset) -> Unit,
    onUnselect: () -> Unit,
    effectsState: MapEffectsState,
    hudState: HUDState,
    overlayState: MapOverlayState.UiStateMap,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxSize()) {
        MapSideBar(
            isPlayerTurn = overlayState.isPlayerTurn,
            logs = overlayState.logs,
            sprintEnabled = hudState.sprintEnabled,
            onBack = onBack,
            onEndTurn = onEndTurn,
            onSprintChecked = onSprintChecked,
            modifier = Modifier.fillMaxWidth(0.2f)
        )
        MapContainer(
            previewPath = hudState.previewPath,
            selectedCharacter = hudState.selectedCharacter,
            pings = effectsState.pings,
            characters = overlayState.characters,
            errorMessage = overlayState.errorMessage,
            onMapClick = onMapClick,
            onUnselect = onUnselect
        )
    }
}

@Composable
private fun MapContainer(
    previewPath: List<Offset>,
    selectedCharacter: Character?,
    pings: List<MapEffect.Ping>,
    characters: List<Character>,
    errorMessage: UiText?,
    onMapClick: (Offset) -> Unit,
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mapTransformState = remember { MapTransformState() }
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.small)
            .background(MaterialTheme.colorScheme.background)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        MapContent(
            previewPath = previewPath,
            mapTransformState = mapTransformState,
            characters = characters,
            selectedCharacter = selectedCharacter,
            pings = pings,
            onMapClick = onMapClick,
            onUnselect = onUnselect
        )
        MapControls(
            mapTransformState = mapTransformState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.TopStart)
                .padding(MaterialTheme.spacing.small)
        ) {
            errorMessage?.let { error ->
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

@Composable
private fun MapControls(mapTransformState: MapTransformState, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { mapTransformState.zoomIn() }) {
                Text("Zoom In")
            }
            Button(onClick = { mapTransformState.reset() }) {
                Text("Reset")
            }
            Button(onClick = { mapTransformState.zoomOut() }) {
                Text("Zoom Out")
            }
        }
    }
}

data class MapTransformer(
    val imageWidth: Int,
    val imageHeight: Int
) {
    private fun toMapOffset(
        x: Int,
        y: Int,
        size: Size
    ) = Offset(
        (x / imageWidth.toFloat()) * size.width,
        (y / imageHeight.toFloat()) * size.height
    )

    private fun toMapOffset(value: Int, size: Size): Float = (value / imageWidth.toFloat()) * size.width

    fun DrawScope.toMapOffset(value: Int) = toMapOffset(value, size)
    fun DrawScope.toMapOffset(x: Int, y: Int) = toMapOffset(x, y, size)

    fun PointerInputScope.toMapOffset(x: Int, y: Int) = toMapOffset(x, y, size.toSize())

    private fun toAbsoluteOffset(
        x: Int,
        y: Int,
        size: Size
    ) = Offset(
        (x / size.width) * imageWidth,
        (y / size.height) * imageHeight
    )

    fun DrawScope.toAbsoluteOffset(x: Int, y: Int) = toAbsoluteOffset(x, y, size)

    fun PointerInputScope.toAbsoluteOffset(x: Int, y: Int) = toAbsoluteOffset(x, y, size.toSize())
}

@Composable
private fun MapContent(
    previewPath: List<Offset>,
    mapTransformState: MapTransformState,
    characters: List<Character>,
    selectedCharacter: Character?,
    pings: List<MapEffect.Ping>,
    onMapClick: (Offset) -> Unit,
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
            .graphicsLayer {
                scaleX = mapTransformState.scale
                scaleY = mapTransformState.scale
                translationX = mapTransformState.offset.x
                translationY = mapTransformState.offset.y
            }
            .detectDragMap { dragAmount -> mapTransformState.applyPan(dragAmount) }
            .detectZoom { delta -> mapTransformState.applyZoom(delta) }
    ) {
        val mapImage = imageResource(Res.drawable.defile1)
        val transformer: MapTransformer = remember(mapImage) {
            MapTransformer(mapImage.width, mapImage.height)
        }

        Image(
            bitmap = mapImage,
            contentDescription = "Zoomable Image",
            contentScale = ContentScale.Inside,
            modifier = Modifier.fillMaxSize()
        )
        CharactersOverlay(
            previewPath = previewPath,
            characters = characters,
            transformer = transformer,
            selectedCharacter = selectedCharacter,
            onMapClick = onMapClick,
            onUnselect = onUnselect,
            modifier = Modifier.matchParentSize()
        )
        AnimationsOverlay(
            pings = pings,
            transformer = transformer,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
private fun CharactersOverlay(
    previewPath: List<Offset>,
    characters: List<Character>,
    transformer: MapTransformer,
    selectedCharacter: Character?,
    onMapClick: (Offset) -> Unit,
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var circlePosition by remember { mutableStateOf(Offset.Zero) }
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                handleMapGestures(
                    mapTransformer = transformer,
                    onMapClick = onMapClick,
                    onUnselect = onUnselect
                ) { newPosition ->
                    circlePosition = newPosition
                }
            }
    ) {
        selectedCharacter?.let {
            drawMovementIndicator(
                previewPath = previewPath,
                speed = it.speed,
                circlePosition = circlePosition,
                transformer = transformer,
                textMeasurer = textMeasurer
            )
        }
        drawCharacters(characters, selectedCharacter, transformer)
    }
}

fun DrawScope.drawReachableMovement(start: Offset, end: Offset, color: Color, strokeWidth: Float = 2f) {
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

fun DrawScope.drawMovementIndicator(
    previewPath: List<Offset>,
    speed: Int,
    circlePosition: Offset,
    textMeasurer: TextMeasurer,
    transformer: MapTransformer
) = with(transformer) {
    if (previewPath.isEmpty()) return@with
    val mapScale = 30f
    var distance = 0f

    previewPath.windowed(2).forEach { (startRaw, endRaw) ->
        val start = toMapOffset(startRaw.x.toInt(), startRaw.y.toInt())
        val end = toMapOffset(endRaw.x.toInt(), endRaw.y.toInt())

        val segmentDistance = start.getDistanceTo(end) / mapScale
        if (distance + segmentDistance < speed) {
            drawReachableMovement(start, end, Color.Green)
        }
        distance += segmentDistance
    }

    val lastPoint = toMapOffset(previewPath.last().x.toInt(), previewPath.last().y.toInt())
    val targetPoint = Offset(circlePosition.x, circlePosition.y)
    val remainingDistance = lastPoint.getDistanceTo(targetPoint) / mapScale

    val midPoint = Offset((lastPoint.x + targetPoint.x) / 2, (lastPoint.y + targetPoint.y) / 2)

    if (distance + remainingDistance < speed) {
        drawReachableMovement(lastPoint, targetPoint, Color.Green)
    } else {
        val maxReachable = computeMaxReachableOffset(lastPoint, targetPoint, speed - distance, remainingDistance)
        drawReachableMovement(lastPoint, maxReachable, Color.Green)
        drawUnReachableMovement(maxReachable, targetPoint)
    }

    drawText(
        textMeasurer = textMeasurer,
        text = "${(distance + remainingDistance).roundToDecimals(1)}m",
        topLeft = midPoint
    )
}

private fun computeMaxReachableOffset(start: Offset, end: Offset, remainingSpeed: Float, segmentDistance: Float): Offset {
    if (segmentDistance <= 0f) return start

    val factor = remainingSpeed / segmentDistance
    return Offset(
        start.x + (end.x - start.x) * factor,
        start.y + (end.y - start.y) * factor
    )
}


fun DrawScope.drawUnReachableMovement(start: Offset, end: Offset) {
    drawLine(
        color = Color.Gray,
        start = start,
        end = end,
        strokeWidth = 2f
    )
}

@Composable
private fun AnimationsOverlay(
    pings: List<MapEffect.Ping>,
    transformer: MapTransformer,
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

private suspend fun PointerInputScope.handleMapGestures(
    mapTransformer: MapTransformer,
    onMapClick: (Offset) -> Unit,
    onUnselect: () -> Unit,
    updatePointerPosition: (Offset) -> Unit
) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            event.changes.firstOrNull()?.let { pointerMovement ->
                updatePointerPosition(pointerMovement.position)
            }
            if (event.type == PointerEventType.Press &&
                event.buttons.isSecondaryPressed) {
                event.changes.forEach { e -> e.consume() }
                onUnselect()
            }
            event.changes.firstOrNull { it.changedToUp() } ?.let { pointerTap ->
                with(mapTransformer) {
                    val center = toAbsoluteOffset(pointerTap.position.x.toInt(), pointerTap.position.y.toInt())
                    onMapClick(center)
                }
            }
        }
    }
}

private fun Modifier.detectDragMap(onDrag: (Offset) -> Unit): Modifier =
    pointerInput(Unit) {
        detectDragGesturesAfterLongPress { change, dragAmount ->
            change.consume()
            onDrag(dragAmount)
        }
    }

private fun Modifier.detectZoom(onZoom: (Offset) -> Unit): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            event.changes.forEach { pointerInputChange ->
                pointerInputChange.scrollDelta.takeIf { it.y != 0f }?.let { delta ->
                    pointerInputChange.consume()
                    onZoom(delta)
                }
            }
        }
    }
}

@Stable
data class MapTransformState(
    var initialScale: Float = 1f,
    var initialOffset: Offset = Offset.Zero,
    val zoomFactor: Float = 1.2f,
    val minScale: Float = 0.6f,
    val maxScale: Float = 4f
) {
    var scale by mutableStateOf(initialScale)
        private set

    var offset by mutableStateOf(initialOffset)
        private set

    fun applyZoom(delta: Offset) {
        if (delta.y > 0)
            zoomIn()
        else
            zoomOut()
    }

    fun zoomIn() {
        scale = (scale * zoomFactor).coerceIn(minScale, maxScale)
    }

    fun zoomOut() {
        scale = (scale * 1 / zoomFactor).coerceIn(minScale, maxScale)
    }

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    fun applyPan(dragAmount: Offset) {
        offset += dragAmount * scale
    }
}

@Composable
private fun MapSideBar(
    isPlayerTurn: Boolean,
    logs: List<String>,
    sprintEnabled: Boolean,
    onBack: () -> Unit,
    onEndTurn: () -> Unit,
    onSprintChecked: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
            .padding(MaterialTheme.spacing.small)
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }
        Button(
            onClick = onEndTurn,
            enabled = isPlayerTurn
        ) {
            Text("End turn")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Sprint",
                color = MaterialTheme.colorScheme.primary.copy(
                    if (isPlayerTurn) 1f else 0.4f
                )
            )
            Checkbox(
                checked = sprintEnabled,
                onCheckedChange = onSprintChecked,
                enabled = isPlayerTurn
            )
        }
        Text("History:")
        LazyColumn(
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary)
                .padding(horizontal = MaterialTheme.spacing.small)
                .fillMaxSize()
        ) {
            items(logs.size) { index ->
                Text(logs[index])
            }
        }
    }
}

private fun String.toColor(): Color = when(this) {
    "green" -> Color.Green
    else -> Color.Gray
}

private fun DrawScope.drawPings(
    pings: List<MapEffect.Ping>,
    mapTransformer: MapTransformer,
    radius: Float,
    pingColor: Color
) {
    pings.forEach { ping ->
        with(mapTransformer) {
            drawCircle(
                color = pingColor,
                radius = radius,
                center = toMapOffset(ping.x, ping.y)
            )
        }
    }
}

fun Offset.getDistanceTo(target: Offset): Float {
    return this.minus(target).getDistance()
}

private fun DrawScope.drawCharacters(
    characters: List<Character>,
    selectedCharacter: Character?,
    mapTransformer: MapTransformer
) {
    with(mapTransformer) {
        characters.forEach { character ->
            val center = toMapOffset(character.x, character.y)
            val radius = if (character == selectedCharacter) toMapOffset(SELECTED_CHARACTER_RADIUS) else toMapOffset(CHARACTER_RADIUS)
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
        }
    }
}
