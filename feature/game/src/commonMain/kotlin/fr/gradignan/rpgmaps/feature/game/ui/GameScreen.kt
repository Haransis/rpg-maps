package fr.gradignan.rpgmaps.feature.game.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.gradignan.rpgmaps.core.ui.compose.CenteredErrorText
import fr.gradignan.rpgmaps.core.ui.compose.CenteredProgressIndicator
import fr.gradignan.rpgmaps.core.ui.compose.ReorderableList
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.game.model.GameIntent
import fr.gradignan.rpgmaps.feature.game.model.GameState
import fr.gradignan.rpgmaps.feature.game.model.GmState
import fr.gradignan.rpgmaps.feature.game.model.HUDState
import fr.gradignan.rpgmaps.feature.game.ui.gm.GmToolBox
import fr.gradignan.rpgmaps.feature.game.ui.map.MapContainer
import fr.gradignan.rpgmaps.feature.game.ui.viewmodel.GameViewModel
import fr.gradignan.rpgmaps.feature.game.ui.viewmodel.GmViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


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
    val gameState: GameState by viewModel.gameState.collectAsStateWithLifecycle()
    val gmState: GmState by gmViewModel.gmState.collectAsStateWithLifecycle()

    when (val state = gameState) {
        GameState.Loading -> {
            CenteredProgressIndicator(modifier)
        }
        is GameState.Error -> {
            CenteredErrorText(state.error.asString(), modifier)
        }
        is GameState.Active -> {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl ) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr ) {
                            when (val newState = gmState) {
                                GmState.Loading -> CenteredProgressIndicator()
                                is GmState.Error -> CenteredErrorText(newState.error.asString())
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
                                CenterAlignedTopAppBar(
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
                                        if (state.mapState.isAdmin) {
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
                            Row(modifier.padding(innerPadding).fillMaxSize()) {
                                SideHud(
                                    hudState = state.hudState,
                                    action = viewModel::processIntent,
                                    modifier = Modifier.fillMaxWidth(0.2f),
                                )
                                MapContainer(
                                    mapState = state.mapState,
                                    action = viewModel::processIntent,
                                    modifier = Modifier.fillMaxHeight().weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SideHud(
    hudState: HUDState,
    action: (GameIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
    ) {
        Text("Characters:")
        ReorderableList(
            hasFullControl = hudState.isAdmin,
            items = hudState.characters,
            onDelete = { action(GameIntent.DeleteChar(it)) },
            onOrderChange = { action(GameIntent.ChangeInitiative(it)) },
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
            items(hudState.logs.size) { index ->
                Text(hudState.logs[index])
            }
        }
        Button(
            onClick = { action(GameIntent.EndTurn) },
            enabled = hudState.isPlayerTurn,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        ) {
            Text("End turn")
        }
    }
}
