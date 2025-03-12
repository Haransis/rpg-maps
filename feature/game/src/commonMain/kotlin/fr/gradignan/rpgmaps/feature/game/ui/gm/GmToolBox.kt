package fr.gradignan.rpgmaps.feature.game.ui.gm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.gradignan.rpgmaps.core.model.Board
import fr.gradignan.rpgmaps.core.model.DataCharacter
import fr.gradignan.rpgmaps.core.ui.compose.StringSelector
import fr.gradignan.rpgmaps.core.ui.theme.spacing


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
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
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
