package fr.gradignan.rpgmaps.feature.game.ui.map

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

@Stable
internal class TransformState(
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero,
) {
    private val zoomFactor: Float = 1.2f
    private val minScale: Float = 0.6f
    private val maxScale: Float = 4f

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
