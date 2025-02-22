package fr.gradignan.rpgmaps.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.gradignan.rpgmaps.core.model.Room
import fr.gradignan.rpgmaps.core.ui.error.UiText
import fr.gradignan.rpgmaps.core.ui.theme.size
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.home.model.HomeUiState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import rpg_maps.feature.home.generated.resources.Res
import rpg_maps.feature.home.generated.resources.create_game
import rpg_maps.feature.home.generated.resources.gm_options
import rpg_maps.feature.home.generated.resources.rooms


@Composable
fun HomeScreen(
    onRoomClick: (Room) -> Unit,
    onCreateMapClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel<HomeViewModel>()
) {
    Box (
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(MaterialTheme.spacing.extraLarge)
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()
        when (val uiState = state) {
            is HomeUiState.Error -> Text(
                color = MaterialTheme.colorScheme.error,
                text = uiState.message
            )
            HomeUiState.Loading -> CircularProgressIndicator()
            is HomeUiState.Success -> HomeScreenContent(
                username = uiState.username,
                rooms = uiState.rooms,
                errorMessage = uiState.errorMessage,
                isLoadingRooms = uiState.isLoadingRooms,
                onRoomClick = onRoomClick,
                onCreateMapClick = onCreateMapClick
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    username: String,
    rooms: List<Room>,
    errorMessage: UiText?,
    isLoadingRooms: Boolean,
    onRoomClick: (Room) -> Unit,
    onCreateMapClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = stringResource(Res.string.rooms),
            style = MaterialTheme.typography.titleLarge
        )
        RoomSection(
            rooms = rooms,
            errorMessage = errorMessage,
            isLoadingRooms = isLoadingRooms,
            onRoomClick = onRoomClick
        )
        Text(
            text = stringResource(Res.string.gm_options),
            style = MaterialTheme.typography.titleLarge
        )
        Button(onClick = onCreateMapClick) {
            Text(stringResource(Res.string.create_game))
        }
    }
}

@Composable
fun RoomSection(
    rooms: List<Room>,
    errorMessage: UiText?,
    isLoadingRooms: Boolean,
    onRoomClick: (Room) -> Unit,
    modifier: Modifier = Modifier
) {
    Box (
        contentAlignment = Alignment.Center,
        modifier = modifier.height(200.dp).fillMaxWidth()
    ) {
        if (isLoadingRooms) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(
                color = MaterialTheme.colorScheme.error,
                text = errorMessage.asString()
            )
        } else {
            RoomList(
                rooms = rooms,
                onRoomClick = onRoomClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RoomList(
    rooms: List<Room>,
    onRoomClick: (Room) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier) {
        items(rooms.size) { index ->
            RoomItem(
                room = rooms[index],
                onClick = { onRoomClick(rooms[index]) }
            )
        }
    }
}

@Composable
fun RoomItem(room: Room, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(CornerSize(MaterialTheme.size.extraSmall)),
        modifier = modifier
            .fillMaxHeight()
            .width(200.dp)
            .padding(MaterialTheme.spacing.small)
            .clickable { onClick() }
    ) {
        Column  {
            Text(room.name)
            Text(room.roleInRoom)
        }
    }
}
