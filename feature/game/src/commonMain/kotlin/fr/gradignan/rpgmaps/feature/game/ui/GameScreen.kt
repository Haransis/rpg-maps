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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import fr.gradignan.rpgmaps.core.common.format
import fr.gradignan.rpgmaps.core.model.Board
import fr.gradignan.rpgmaps.core.model.DataCharacter
import fr.gradignan.rpgmaps.core.model.MapCharacter
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.ui.error.UiText
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.game.model.GameState
import fr.gradignan.rpgmaps.feature.game.model.PreviewPath
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val PING_RADIUS_PX = 20f
private const val SELECTED_CHARACTER_RADIUS = 25
const val CHARACTER_RADIUS = 20

@Composable
fun GameScreenRoute(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
    val gameState: GameState by viewModel.gameState.collectAsStateWithLifecycle()

    when (val state = gameState) {
        GameState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
        is GameState.Error -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                Text(
                    text = state.error.asString(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        is GameState.Game -> {
            GameContent(
                gameState = state,
                onBack = onBack,
                onBoardSubmit = viewModel::onBoardSubmit,
                onCharacterSelect = viewModel::onCharacterSelect,
                onCharacterSubmit = viewModel::onCharacterSubmit,
                onDeleteChar = viewModel::onDeleteChar,
                onChangeInitiative = viewModel::onChangeInitiative,
                onStartGame = viewModel::onStartGame,
                onBoardSelect = viewModel::onBoardSelect,
                onEndTurn = viewModel::onEndTurn,
                onGmCheck = viewModel::onGmCheck,
                onSprintCheck = viewModel::onSprintCheck,
                onMapClick = viewModel::onMapClick,
                onPointerMove = viewModel::onPointerMove,
                onDoubleClick = viewModel::onDoubleClick,
                onUnselect = viewModel::onUnselect,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun GameContent(
    gameState: GameState.Game,
    onBoardSubmit: () -> Unit,
    onCharacterSelect: (String) -> Unit,
    onCharacterSubmit: () -> Unit,
    onDeleteChar: (Int) -> Unit,
    onChangeInitiative: (ItemMove) -> Unit,
    onStartGame: () -> Unit,
    onBack: () -> Unit,
    onBoardSelect: (String) -> Unit,
    onEndTurn: () -> Unit,
    onGmCheck: (Boolean) -> Unit,
    onSprintCheck: (Boolean) -> Unit,
    onMapClick: (Offset) -> Unit,
    onPointerMove: (Offset) -> Unit,
    onDoubleClick: () -> Unit,
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxSize()) {
        MapSideBar(
            isPlayerTurn = gameState.isPlayerTurn,
            logs = gameState.logs,
            isSprintChecked = gameState.isSprintChecked,
            onBack = onBack,
            onEndTurn = onEndTurn,
            onSprintCheck = onSprintCheck,
            modifier = Modifier.fillMaxWidth(0.2f)
        )
        MapContainer(
            imageUrl = gameState.imageUrl,
            previewPath = gameState.previewPath,
            hoveredCharacterId = gameState.hoveredCharacterId,
            selectedMapCharacter = gameState.selectedMapCharacter,
            pings = gameState.pings,
            mapCharacters = gameState.mapCharacters,
            errorMessage = gameState.error,
            onMapClick = onMapClick,
            onPointerMove = onPointerMove,
            onDoubleClick = onDoubleClick,
            onUnselect = onUnselect,
            modifier = Modifier.fillMaxHeight().weight(1f)
        )
        if (gameState.isAdmin) {
            GmToolBox(
                isGmChecked = gameState.isGmChecked,
                selectedBoard = gameState.selectedBoard,
                boards = gameState.boards,
                selectedCharacter = gameState.selectedChar,
                availableCharacters = gameState.availableCharacters,
                mapCharacters = gameState.mapCharacters,
                onBoardSubmit = onBoardSubmit,
                onCharacterSelect = onCharacterSelect,
                onCharacterSubmit = onCharacterSubmit,
                onChangeInitiative = onChangeInitiative,
                onDelete = onDeleteChar,
                onStartGame = onStartGame,
                onBoardSelect = onBoardSelect,
                onGmCheck = onGmCheck,
                modifier = Modifier.fillMaxWidth(0.3f),
            )
        }
    }
}

@Composable
fun GmToolBox(
    selectedBoard: Board?,
    boards: List<Board>,
    selectedCharacter: String?,
    availableCharacters: List<DataCharacter>,
    mapCharacters: List<MapCharacter>,
    isGmChecked: Boolean,
    onBoardSelect: (String) -> Unit,
    onBoardSubmit: () -> Unit,
    onChangeInitiative: (ItemMove) -> Unit,
    onCharacterSelect: (String) -> Unit,
    onCharacterSubmit: () -> Unit,
    onDelete: (Int) -> Unit,
    onStartGame: () -> Unit,
    onGmCheck: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
    ) {
        Text("Gm Toolbox", style = MaterialTheme.typography.titleMedium)
        CheckBoxText(
            text = "Gm mode",
            checked = isGmChecked,
            onCheck = onGmCheck,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        )
        StringSelector(
            selectedOptions = selectedBoard?.name,
            options = boards.map { it.name },
            placeHolder = "Choose a map",
            onOptionSelect = onBoardSelect,
        )
        Button(
            enabled = selectedBoard != null,
            onClick = onBoardSubmit,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        ) {
            Text("Load")
        }
        StringSelector(
            selectedOptions = selectedCharacter,
            options = availableCharacters.map { it.name },
            placeHolder = "Choose a character",
            onOptionSelect = onCharacterSelect,
        )
        Button(
            enabled = selectedCharacter != null,
            onClick = onCharacterSubmit,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        ) {
            Text("Add")
        }
        ReorderableList(
            items = mapCharacters.mapIndexed { index, item -> Item(index, item.name, item.cmId) },
            onDelete = onDelete,
            onChangeInitiative = onChangeInitiative,
            modifier = Modifier
                .padding(bottom = MaterialTheme.spacing.medium)
                .weight(1f, fill = false)
        )
        Button(onClick = onStartGame) {
            Text("Start Game")
        }
    }
}

data class ItemMove(val from: Int, val to: Int)
data class Item(val index: Int, val name: String, val optionalId: Int? = null)

@Composable
fun ReorderableList(
    items: List<Item>,
    onChangeInitiative: (ItemMove) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyColumnState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onChangeInitiative(ItemMove(from = from.index, to = to.index))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.small),
        state = lazyListState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(items, key = { _, item -> item.index }) { _, item ->
            ReorderableItem(reorderableLazyColumnState, item.index) {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxSize().draggableHandle()
                ){
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DragHandle,
                            contentDescription = "Reorder",
                        )
                        val label = if (item.optionalId == null) item.name else "${item.name} - ${item.optionalId}"
                        Text(label, Modifier.weight(1f))
                        IconButton(
                            onClick = { onDelete(item.index) }
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringSelector(
    options: List<String>,
    selectedOptions: String?,
    placeHolder: String,
    onOptionSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.small)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .clickable { expanded = true }
                .pointerHoverIcon(PointerIcon.Default)
                .padding(MaterialTheme.spacing.small)
        ) {
            Text(
                color = MaterialTheme.colorScheme.primary.copy(
                    if (selectedOptions != null) 1f else 0.4f
                ),
                text = selectedOptions ?: placeHolder,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            options.forEach { board ->
                DropdownMenuItem(
                    text = { Text(board) },
                    onClick = {
                        onOptionSelect(board)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MapContainer(
    imageUrl: String?,
    previewPath: PreviewPath,
    hoveredCharacterId: Int?,
    selectedMapCharacter: MapCharacter?,
    pings: List<MapEffect.Ping>,
    mapCharacters: List<MapCharacter>,
    errorMessage: UiText?,
    onMapClick: (Offset) -> Unit,
    onPointerMove: (Offset) -> Unit,
    onDoubleClick: () -> Unit,
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mapTransformState = remember { MapTransformState() }
    Box(
        modifier = modifier
            .padding(MaterialTheme.spacing.small)
            .background(MaterialTheme.colorScheme.background)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            MapContent(
                imageUrl = imageUrl,
                previewPath = previewPath,
                mapTransformState = mapTransformState,
                mapCharacters = mapCharacters,
                hoveredCharacterId = hoveredCharacterId,
                selectedMapCharacter = selectedMapCharacter,
                pings = pings,
                onMapClick = onMapClick,
                onPointerMove = onPointerMove,
                onDoubleClick = onDoubleClick,
                onUnselect = onUnselect,
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
        } else {
            if (errorMessage != null) {
                Text(
                    color = MaterialTheme.colorScheme.error,
                    text = errorMessage.asString()
                )
            } else {
                Text("Waiting for game master...")
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
        x: Float,
        y: Float,
        size: Size
    ) = Offset(
        (x / imageWidth.toFloat()) * size.width,
        (y / imageHeight.toFloat()) * size.height
    )

    private fun toMapOffset(value: Int, size: Size): Float = (value / imageWidth.toFloat()) * size.width

    fun DrawScope.toMapOffset(value: Int) = toMapOffset(value, size)
    fun DrawScope.toMapOffset(x: Int, y: Int) = toMapOffset(x.toFloat(), y.toFloat(), size)
    fun DrawScope.toMapOffset(offset: Offset) = toMapOffset(offset.x, offset.y, size)

    private fun toAbsoluteOffset(
        x: Float,
        y: Float,
        size: Size
    ) = Offset(
        (x / size.width) * imageWidth,
        (y / size.height) * imageHeight
    )

    fun PointerInputScope.toAbsoluteOffset(offset: Offset) = toAbsoluteOffset(offset.x, offset.y, size.toSize())
}

@Composable
private fun MapContent(
    imageUrl: String?,
    previewPath: PreviewPath,
    mapTransformState: MapTransformState,
    mapCharacters: List<MapCharacter>,
    hoveredCharacterId: Int?,
    selectedMapCharacter: MapCharacter?,
    pings: List<MapEffect.Ping>,
    onMapClick: (Offset) -> Unit,
    onPointerMove: (Offset) -> Unit,
    onDoubleClick: () -> Unit,
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
        val painter = rememberAsyncImagePainter(
            imageUrl, contentScale = ContentScale.Inside
        )

        val painterState by painter.state.collectAsState()
        when (val state = painterState) {
            AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Loading -> CircularProgressIndicator()
            is AsyncImagePainter.State.Success -> {
                val transformer: MapTransformer = remember(state) {
                    MapTransformer(state.result.image.width, state.result.image.height)
                }
                Image(
                    painter = painter,
                    contentDescription = "Zoomable Image",
                    contentScale = ContentScale.Inside,
                    modifier = Modifier.fillMaxSize()
                )
                CharactersOverlay(
                    previewPath = previewPath,
                    mapCharacters = mapCharacters,
                    transformer = transformer,
                    hoveredCharacterId = hoveredCharacterId,
                    selectedMapCharacter = selectedMapCharacter,
                    onMapClick = onMapClick,
                    onDoubleClick = onDoubleClick,
                    onPointerMove = onPointerMove,
                    onUnselect = onUnselect,
                    modifier = Modifier.matchParentSize()
                        .pointerHoverIcon(if (hoveredCharacterId != null) PointerIcon.Hand else PointerIcon.Default)
                )
                AnimationsOverlay(
                    pings = pings,
                    transformer = transformer,
                    modifier = Modifier.matchParentSize()
                )
            }
            is AsyncImagePainter.State.Error -> Text(text = "Error loading image", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun CharactersOverlay(
    previewPath: PreviewPath,
    mapCharacters: List<MapCharacter>,
    transformer: MapTransformer,
    hoveredCharacterId: Int?,
    selectedMapCharacter: MapCharacter?,
    onMapClick: (Offset) -> Unit,
    onDoubleClick: () -> Unit,
    onPointerMove: (Offset) -> Unit,
    onUnselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                with(transformer) {
                    detectTapGestures (
                        onDoubleTap = {
                            onDoubleClick()
                        },
                        onTap = {
                            onMapClick(toAbsoluteOffset(it))
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                with(transformer) {
                    handleMapGestures(
                        onUnselect = onUnselect,
                        onPointerMove = { newPosition -> onPointerMove(toAbsoluteOffset(newPosition))}
                    )
                }
            }
    ) {
        selectedMapCharacter?.let {
            drawMovementIndicator(
                previewPath = previewPath,
                transformer = transformer,
                textMeasurer = textMeasurer
            )
        }
        drawCharacters(
            mapCharacters = mapCharacters,
            selectedMapCharacter = selectedMapCharacter,
            hoveredCharacterId = hoveredCharacterId,
            mapTransformer = transformer,
            textMeasurer = textMeasurer
        )
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
    previewPath: PreviewPath,
    textMeasurer: TextMeasurer,
    transformer: MapTransformer
) = with(transformer) {
    if (previewPath.reachable.size <= 1) return@with
    previewPath.reachable.windowed(2).forEach { (startRaw, endRaw) ->
        val start = toMapOffset(startRaw)
        val end = toMapOffset(endRaw)
        drawReachableMovement(start, end, Color.Green)
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
        topLeft = toMapOffset(previewPath.unreachableStop ?: previewPath.reachable.last())+Offset(10f, 0f),
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
    onUnselect: () -> Unit,
    onPointerMove: (Offset) -> Unit
) = awaitPointerEventScope {
    while (true) {
        val event = awaitPointerEvent()
        event.changes.firstOrNull()?.let { pointerMovement ->
            onPointerMove(pointerMovement.position)
        }
        if (event.type == PointerEventType.Press &&
            event.buttons.isSecondaryPressed) {
            event.changes.forEach { e -> e.consume() }
            onUnselect()
        }
    }
}


private fun Modifier.detectDragMap(onDrag: (Offset) -> Unit): Modifier =
    pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
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
    isSprintChecked: Boolean,
    onBack: () -> Unit,
    onEndTurn: () -> Unit,
    onSprintCheck: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Button(
            onClick = onEndTurn,
            enabled = isPlayerTurn
        ) {
            Text("End turn")
        }
        CheckBoxText(
            text = "Sprint",
            enabled = isPlayerTurn,
            checked = isSprintChecked,
            onCheck = onSprintCheck
        )
        Text("History:")
        LazyColumn(
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                .padding(horizontal = MaterialTheme.spacing.small)
                .fillMaxSize()
        ) {
            items(logs.size) { index ->
                Text(logs[index])
            }
        }
    }
}

@Composable
private fun CheckBoxText(
    text: String,
    checked: Boolean,
    onCheck: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary.copy(
                if (enabled) 1f else 0.4f
            )
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheck,
            enabled = enabled
        )
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
) = with(mapTransformer) {
    pings.forEach { ping ->
        drawCircle(
            color = pingColor,
            radius = radius,
            center = toMapOffset(ping.x, ping.y)
        )
    }
}


fun Offset.getDistanceTo(target: Offset): Float {
    return this.minus(target).getDistance()
}

private fun DrawScope.drawCharacters(
    mapCharacters: List<MapCharacter>,
    selectedMapCharacter: MapCharacter?,
    hoveredCharacterId: Int?,
    mapTransformer: MapTransformer,
    textMeasurer: TextMeasurer
) {
    with(mapTransformer) {
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
