package fr.gradignan.rpgmaps.feature.game.ui.map

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.unit.toSize


internal class CoordinatesConverter(
    private val imageWidth: Int,
    private val imageHeight: Int
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
