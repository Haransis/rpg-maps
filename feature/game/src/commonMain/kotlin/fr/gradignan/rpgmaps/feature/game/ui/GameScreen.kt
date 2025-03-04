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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.LayoutDirection
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
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.game.model.CharItem
import fr.gradignan.rpgmaps.feature.game.model.MapState
import fr.gradignan.rpgmaps.feature.game.model.DistancePath
import fr.gradignan.rpgmaps.feature.game.model.GmState
import fr.gradignan.rpgmaps.feature.game.model.StatusState
import kotlinx.coroutines.launch
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
    gmViewModel: GmViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    GameScreen(
        onBack = onBack,
        viewModel = viewModel,
        gmViewModel = gmViewModel,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = koinViewModel<GameViewModel>(),
    gmViewModel: GmViewModel = koinViewModel<GmViewModel>()
) {
    val mapState: MapState by viewModel.mapState.collectAsStateWithLifecycle()
    val statusState: StatusState by viewModel.statusState.collectAsStateWithLifecycle()
    val gmState: GmState by gmViewModel.gmState.collectAsStateWithLifecycle()

    when (val state = mapState) {
        MapState.Loading -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
        is MapState.Error -> {
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
        is MapState.Game -> {

            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl ) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr ) {
                            when (val newState = gmState) {
                                GmState.Loading -> CircularProgressIndicator()
                                is GmState.Error -> Text(newState.error.asString())
                                is GmState.Gm ->
                                    GmToolBox(
                                        selectedBoard = newState.selectedBoard,
                                        boards = newState.boards,
                                        selectedCharacter = newState.selectedChar,
                                        availableCharacters = newState.availableCharacters,
                                        onBoardSubmit = gmViewModel::onBoardSubmit,
                                        onCharacterSelect = gmViewModel::onCharacterSelect,
                                        onCharacterSubmit = gmViewModel::onCharacterSubmit,
                                        onStartGame = gmViewModel::onStartGame,
                                        onBoardSelect = gmViewModel::onBoardSelect,
                                        modifier = Modifier.fillMaxWidth(0.3f),
                                    )
                            }
                        }
                    }
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("Game") },
                                    navigationIcon = {
                                        IconButton(onClick = onBack) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                    },
                                    actions = {
                                        if (state.isAdmin) {
                                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                Icon(
                                                    Icons.Default.Menu,
                                                    contentDescription = "Menu"
                                                )
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                                        titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                                        actionIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                                        navigationIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                                    )
                                )
                            }
                        ) { innerPadding ->
                            GameContent(
                                mapState = state,
                                statusState = statusState,
                                onPingCheck = viewModel::onPingCheck,
                                onRulerCheck = viewModel::onRulerCheck,
                                onDeleteChar = viewModel::onDeleteChar,
                                onChangeInitiative = viewModel::onChangeInitiative,
                                onEndTurn = viewModel::onEndTurn,
                                onGmCheck = viewModel::onGmCheck,
                                onSprintCheck = viewModel::onSprintCheck,
                                onMapClick = viewModel::onMapClick,
                                onPointerMove = viewModel::onPointerMove,
                                onDoubleClick = viewModel::onDoubleClick,
                                onUnselect = viewModel::onUnselect,
                                modifier = modifier.padding(innerPadding),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameContent(
    mapState: MapState.Game,
    statusState: StatusState,
    onPingCheck: (Boolean) -> Unit,
    onRulerCheck: (Boolean) -> Unit,
    onDeleteChar: (Int) -> Unit,
    onChangeInitiative: (List<Int>) -> Unit,
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
            statusState = statusState,
            onChangeInitiative = onChangeInitiative,
            onDelete = onDeleteChar,
            onEndTurn = onEndTurn,
            modifier = Modifier.fillMaxWidth(0.2f),
        )
        MapContainer(
            mapState = mapState,
            onPingCheck = onPingCheck,
            onRulerCheck = onRulerCheck,
            onMapClick = onMapClick,
            onPointerMove = onPointerMove,
            onDoubleClick = onDoubleClick,
            onGmCheck = onGmCheck,
            onSprintCheck = onSprintCheck,
            onUnselect = onUnselect,
            modifier = Modifier.fillMaxHeight().weight(1f),
        )
    }
}

@Composable
fun GmToolBox(
    selectedBoard: Board?,
    boards: List<Board>,
    selectedCharacter: String?,
    availableCharacters: List<DataCharacter>,
    onBoardSelect: (String) -> Unit,
    onBoardSubmit: () -> Unit,
    onCharacterSelect: (String) -> Unit,
    onCharacterSubmit: () -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
        ) {
            Text("Gm Toolbox", style = MaterialTheme.typography.titleMedium)
            StringSelector(
                selectedOptions = selectedBoard?.name,
                options = boards.map { it.name },
                placeHolder = "Choose a map",
                onOptionSelect = onBoardSelect,
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
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
            Button(onClick = onStartGame) {
                Text("Start Game")
            }
        }
    }
}

@Composable
fun ReorderableList(
    isAdmin: Boolean,
    items: List<CharItem>,
    onChangeInitiative: (List<Int>) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    var list by remember(items) {
        mutableStateOf(items)
    }
    val reorderableLazyColumnState = rememberReorderableLazyListState(lazyListState) { from, to ->
        list = list.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.small),
        state = lazyListState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(list, key = { _, item -> item.index }) { _, item ->
            ReorderableItem(reorderableLazyColumnState, item.index) {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxSize()
                        .then(if (isAdmin) Modifier.draggableHandle(
                            onDragStopped = {
                                if (list != items) {
                                    onChangeInitiative(list.map { it.index })
                                }
                            }
                        ) else Modifier
                        )

                ){
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        if (isAdmin) {
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = "Reorder",
                            )
                        }
                        val label = if (item.optionalId == null) item.name else "${item.name} - ${item.optionalId}"
                        Text(label, Modifier.weight(1f))
                        if (isAdmin) {
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
    mapState: MapState.Game,
    onPingCheck: (Boolean) -> Unit,
    onRulerCheck: (Boolean) -> Unit,
    onMapClick: (Offset) -> Unit,
    onPointerMove: (Offset) -> Unit,
    onDoubleClick: () -> Unit,
    onUnselect: () -> Unit,
    onGmCheck: (Boolean) -> Unit,
    onSprintCheck: (Boolean) -> Unit,
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
        if (mapState.imageUrl != null) {
            val painter = rememberAsyncImagePainter(
                mapState.imageUrl, contentScale = ContentScale.Inside
            )
            val painterState by painter.state.collectAsState()

            when (val state = painterState) {
                AsyncImagePainter.State.Empty,
                is AsyncImagePainter.State.Loading -> CircularProgressIndicator()
                is AsyncImagePainter.State.Success -> {
                    MapContent(
                        mapState = mapState,
                        painter = painter,
                        painterState = state,
                        mapTransformState = mapTransformState,
                        onPingCheck = onPingCheck,
                        onRulerCheck = onRulerCheck,
                        onMapClick = onMapClick,
                        onPointerMove = onPointerMove,
                        onDoubleClick = onDoubleClick,
                        onUnselect = onUnselect,
                        onGmCheck = onGmCheck,
                        onSprintCheck = onSprintCheck
                    )
                }
                is AsyncImagePainter.State.Error -> Text(text = "Error loading image", color = MaterialTheme.colorScheme.error)
            }
        } else {
            if (mapState.error != null) {
                Text(
                    color = MaterialTheme.colorScheme.error,
                    text = mapState.error.asString()
                )
            } else {
                Text("Waiting for game master...")
            }
        }
    }
}

@Composable
private fun MoveControls(
    isAdmin: Boolean,
    isGmChecked: Boolean,
    isPingChecked: Boolean,
    isRulerChecked: Boolean,
    isSprintChecked: Boolean,
    onGmCheck: (Boolean) -> Unit,
    onPingCheck: (Boolean) -> Unit,
    onRulerCheck: (Boolean) -> Unit,
    onSprintCheck: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.inversePrimary,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            if (isAdmin) {
                SwitchText(
                    text = "Gm mode",
                    checked = isGmChecked,
                    onCheck = onGmCheck,
                    modifier = Modifier.padding(end = MaterialTheme.spacing.small)
                )
            }
            CheckBoxText(
                text = "Sprint",
                checked = isSprintChecked,
                onCheck = onSprintCheck
            )
            CheckBoxText(
                text = "Ping",
                checked = isPingChecked,
                onCheck = onPingCheck
            )
            CheckBoxText(
                text = "Ruler",
                checked = isRulerChecked,
                onCheck = onRulerCheck
            )
        }
    }
}

@Composable
private fun MapControls(
    mapTransformState: MapTransformState,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
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
    mapState: MapState.Game,
    painter: AsyncImagePainter,
    painterState: AsyncImagePainter.State.Success,
    mapTransformState: MapTransformState,
    onGmCheck: (Boolean) -> Unit,
    onSprintCheck: (Boolean) -> Unit,
    onPingCheck: (Boolean) -> Unit,
    onRulerCheck: (Boolean) -> Unit,
    onMapClick: (Offset) -> Unit,
    onPointerMove: (Offset) -> Unit,
    onDoubleClick: () -> Unit,
    onUnselect: () -> Unit,
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
                    scaleX = mapTransformState.scale
                    scaleY = mapTransformState.scale
                    translationX = mapTransformState.offset.x
                    translationY = mapTransformState.offset.y
                }
                .detectDragMap { dragAmount -> mapTransformState.applyPan(dragAmount) }
                .detectZoom { delta -> mapTransformState.applyZoom(delta) }
        ) {
            val transformer: MapTransformer = remember(painterState) {
                MapTransformer(painterState.result.image.width, painterState.result.image.height)
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
                onMapClick = onMapClick,
                onDoubleClick = onDoubleClick,
                onPointerMove = onPointerMove,
                onUnselect = onUnselect,
                modifier = Modifier.matchParentSize()
                    .pointerHoverIcon(if (mapState.hoveredCharacterId != null) PointerIcon.Hand else PointerIcon.Default)
            )
            AnimationsOverlay(
                pings = mapState.pings,
                transformer = transformer,
                modifier = Modifier.matchParentSize()
            )
        }
        MoveControls(
            isAdmin = mapState.isAdmin,
            isGmChecked = mapState.isGmChecked,
            isSprintChecked = mapState.isSprintChecked,
            onSprintCheck = onSprintCheck,
            onGmCheck = onGmCheck,
            isPingChecked = mapState.isPingChecked,
            isRulerChecked = mapState.isRulerChecked,
            onPingCheck = onPingCheck,
            onRulerCheck = onRulerCheck,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        MapControls(
            mapTransformState = mapTransformState,
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

@Composable
private fun CharactersOverlay(
    laserPosition: Offset?,
    ruler: DistancePath,
    previewPath: DistancePath,
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
    previewPath: DistancePath,
    textMeasurer: TextMeasurer,
    transformer: MapTransformer,
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
        topLeft = toMapOffset(previewPath.unreachableStop ?: previewPath.reachable.last())+Offset(10f, 0f),
    )
}

fun DrawScope.drawLaser(
    position: Offset,
    transformer: MapTransformer
) = with(transformer) {
    drawCircle(
        color = Color.Red,
        radius = 6f,
        center = toMapOffset(position)
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
    statusState: StatusState,
    onChangeInitiative: (List<Int>) -> Unit,
    onDelete: (Int) -> Unit,
    onEndTurn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
    ) {
        Text("Characters:")
        ReorderableList(
            isAdmin = statusState.isAdmin,
            items = statusState.characters,
            onDelete = onDelete,
            onChangeInitiative = onChangeInitiative,
            modifier = Modifier
                .padding(bottom = MaterialTheme.spacing.medium)
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
        )
        Text("History:")
        LazyColumn(
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                .padding(horizontal = MaterialTheme.spacing.small)
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(statusState.logs.size) { index ->
                Text(statusState.logs[index])
            }
        }
        Button(
            onClick = onEndTurn,
            enabled = statusState.isPlayerTurn,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        ) {
            Text("End turn")
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
        Checkbox(
            checked = checked,
            onCheckedChange = onCheck,
            enabled = enabled
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary.copy(
                if (enabled) 1f else 0.4f
            )
        )
    }
}

@Composable
private fun SwitchText(
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
        Switch(
            checked = checked,
            onCheckedChange = onCheck,
            enabled = enabled,
            modifier = Modifier.padding(end = MaterialTheme.spacing.small)
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary.copy(
                if (enabled) 1f else 0.4f
            )
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
