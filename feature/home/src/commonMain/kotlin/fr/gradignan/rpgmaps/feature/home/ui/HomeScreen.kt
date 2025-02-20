package fr.gradignan.rpgmaps.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import rpg_maps.feature.home.generated.resources.Res
import rpg_maps.feature.home.generated.resources.create_game
import rpg_maps.feature.home.generated.resources.error_username
import rpg_maps.feature.home.generated.resources.start_game

@Composable
fun HomeScreen(
    onGameClick: (String) -> Unit,
    onCreateMapClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel<HomeViewModel>()
) {
    Surface (
        shape = RoundedCornerShape(5),
        modifier = modifier.padding(MaterialTheme.spacing.extraLarge)
    ) {
        val username = viewModel.username.collectAsStateWithLifecycle()
        when (username.value) {
            is Resource.Error -> Text(stringResource(Res.string.error_username))
            Resource.Loading -> CircularProgressIndicator()
            is Resource.Success -> HomeScreenContent(
                username = (username.value as Resource.Success<String>).data,
                onGameClick = onGameClick,
                onCreateMapClick = onCreateMapClick
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    username: String,
    onGameClick: (String) -> Unit,
    onCreateMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Button(onClick = { onGameClick(username) }) {
            Text(stringResource(Res.string.start_game))
        }
        Button(onClick = onCreateMapClick) {
            Text(stringResource(Res.string.create_game))
        }
    }
}


