package markus.wieland.pushygame.render

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.Sprite
import kotlin.math.abs
import kotlin.math.roundToInt

/** All sprites decoded once from `assets/sprites`. They are tiny pixel-art PNGs. */
class SpriteSheet(private val images: Map<Sprite, ImageBitmap>) {
    operator fun get(sprite: Sprite): ImageBitmap = images.getValue(sprite)

    companion object {
        fun load(assets: AssetManager) = SpriteSheet(
            Sprite.entries.associateWith { sprite ->
                assets.open("sprites/${sprite.fileName}.png").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
            },
        )
    }
}

val LocalSprites = compositionLocalOf<SpriteSheet> { error("No SpriteSheet provided") }

/**
 * Draws a board (terrain layer, then entity layer) as crisp pixel art with a fixed cell aspect ratio.
 * [onCellPressed] is called for taps and for every cell a drag passes over (used by the editor).
 */
@Composable
fun BoardCanvas(
    sprites: SpriteSheet,
    terrain: Grid<List<Sprite>>,
    entities: Grid<List<Sprite>>,
    modifier: Modifier = Modifier,
    onCellPressed: ((Coordinate, isDragStart: Boolean) -> Unit)? = null,
) {
    val onCell = rememberUpdatedState(onCellPressed)
    val width = terrain.width
    val height = terrain.height

    fun cellAt(offset: Offset, size: IntSize): Coordinate? {
        val col = (offset.x / size.width * width).toInt()
        val row = (offset.y / size.height * height).toInt()
        return Coordinate(row, col).takeIf { row in 0 until height && col in 0 until width }
    }

    var input = modifier.aspectRatio(width.toFloat() / height)
    if (onCellPressed != null) {
        input = input
            .pointerInput(width, height) {
                detectTapGestures { offset -> cellAt(offset, size)?.let { onCell.value?.invoke(it, true) } }
            }
            .pointerInput(width, height) {
                var last: Coordinate? = null
                detectDragGestures(
                    onDragStart = { offset ->
                        last = cellAt(offset, size)
                        last?.let { onCell.value?.invoke(it, true) }
                    },
                    onDrag = { change, _ ->
                        val cell = cellAt(change.position, size)
                        val previous = last
                        if (cell != null && cell != previous) {
                            last = cell
                            // Fast drags can skip cells – visit every cell on the line in between.
                            val steps = if (previous == null) 0 else maxOf(abs(cell.row - previous.row), abs(cell.col - previous.col))
                            for (i in 1..steps) {
                                val t = i.toFloat() / steps
                                onCell.value?.invoke(
                                    Coordinate(
                                        (previous!!.row + (cell.row - previous.row) * t).roundToInt(),
                                        (previous.col + (cell.col - previous.col) * t).roundToInt(),
                                    ),
                                    false,
                                )
                            }
                            if (steps == 0) onCell.value?.invoke(cell, false)
                        }
                    },
                )
            }
    }

    Canvas(input) {
        val cellWidth = size.width / width
        val cellHeight = size.height / height
        fun drawLayer(layer: Grid<List<Sprite>>) {
            for (c in layer.coordinates) {
                val sprites0 = layer[c]
                if (sprites0.isEmpty()) continue
                val left = (c.col * cellWidth).roundToInt()
                val top = (c.row * cellHeight).roundToInt()
                val dstSize = IntSize(
                    ((c.col + 1) * cellWidth).roundToInt() - left,
                    ((c.row + 1) * cellHeight).roundToInt() - top,
                )
                for (sprite in sprites0) {
                    val image = sprites[sprite]
                    drawImage(
                        image = image,
                        srcSize = IntSize(image.width, image.height),
                        dstOffset = IntOffset(left, top),
                        dstSize = dstSize,
                        filterQuality = FilterQuality.None,
                    )
                }
            }
        }
        drawLayer(terrain)
        drawLayer(entities)
    }
}
