package fr.gradignan.rpgmaps.core.ui.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import fr.gradignan.rpgmaps.core.ui.theme.spacing

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
