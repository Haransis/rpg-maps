package fr.gradignan.rpgmaps.feature.game.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import fr.gradignan.rpgmaps.core.ui.compose.ErrorText
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.game.model.GameIntent
import fr.gradignan.rpgmaps.feature.game.model.MapState


@Composable
internal fun MapContainer(
    mapState: MapState,
    action: (GameIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val transformState = remember { TransformState() }
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
                        transformState = transformState,
                        action = action
                    )
                }
                is AsyncImagePainter.State.Error -> ErrorText(text = "Error loading image")
            }
        } else {
            if (mapState.error != null) {
                ErrorText(mapState.error.asString())
            } else {
                Text("Waiting for game master...")
            }
        }
    }
}
