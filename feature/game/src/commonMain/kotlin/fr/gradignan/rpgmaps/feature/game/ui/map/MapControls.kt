package fr.gradignan.rpgmaps.feature.game.ui.map

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import fr.gradignan.rpgmaps.core.ui.compose.CheckBoxText
import fr.gradignan.rpgmaps.core.ui.compose.SwitchText
import fr.gradignan.rpgmaps.core.ui.theme.spacing
import fr.gradignan.rpgmaps.feature.game.model.GameIntent


@Composable
internal fun SettingsControls(
    isAdmin: Boolean,
    isGmChecked: Boolean,
    isPingChecked: Boolean,
    isRulerChecked: Boolean,
    isSprintChecked: Boolean,
    action: (GameIntent) -> Unit,
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
                    onCheck = { action(GameIntent.GmCheck(it)) },
                    modifier = Modifier.padding(end = MaterialTheme.spacing.small)
                )
            }
            CheckBoxText(
                text = "Sprint",
                checked = isSprintChecked,
                onCheck = { action(GameIntent.SprintCheck(it)) }
            )
            CheckBoxText(
                text = "Ping",
                checked = isPingChecked,
                onCheck = { action(GameIntent.PingCheck(it)) }
            )
            CheckBoxText(
                text = "Ruler",
                checked = isRulerChecked,
                onCheck = { action(GameIntent.RulerCheck(it)) }
            )
        }
    }
}

@Composable
internal fun MapControls(
    transformState: TransformState,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Button(onClick = { transformState.zoomIn() }) {
            Text("Zoom In")
        }
        Button(onClick = { transformState.reset() }) {
            Text("Reset")
        }
        Button(onClick = { transformState.zoomOut() }) {
            Text("Zoom Out")
        }
    }
}

internal suspend fun PointerInputScope.handleMapGestures(
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


internal fun Modifier.detectDragMap(onDrag: (Offset) -> Unit): Modifier =
    pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            onDrag(dragAmount)
        }
    }

internal fun Modifier.detectZoom(onZoom: (Offset) -> Unit): Modifier = pointerInput(Unit) {
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
