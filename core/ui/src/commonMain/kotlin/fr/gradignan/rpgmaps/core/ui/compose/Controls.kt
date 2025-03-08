package fr.gradignan.rpgmaps.core.ui.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.gradignan.rpgmaps.core.ui.theme.spacing

@Composable
fun CheckBoxText(
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
fun SwitchText(
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
