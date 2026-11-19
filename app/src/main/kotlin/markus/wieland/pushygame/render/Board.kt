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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.Sprite
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * All sprites decoded once from `assets/sprites`. They are tiny pixel-art PNGs.
 *
 * `<name>.png` is frame 0 (the only one the web editor knows about); optional animation frames
 * live next to it as `<name>@1.png`, `<name>@2.png`, …
 */
class SpriteSheet(private val frames: Map<Sprite, List<ImageBitmap>>) {

    /** First frame – used for icons, the editor and previews. */
    operator fun get(sprite: Sprite): ImageBitmap = frames.getValue(sprite)[0]

    /** The frame of [sprite] to show at [timeMs]. */
    fun frame(sprite: Sprite, timeMs: Long): ImageBitmap {
        val list = frames.getValue(sprite)
        if (list.size == 1) return list[0]
        return list[((timeMs / frameDurationMs(sprite)) % list.size).toInt()]
    }

    val hasAnimations: Boolean = frames.values.any { it.size > 1 }

    companion object {
        fun load(assets: AssetManager): SpriteSheet {
            val files = assets.list("sprites").orEmpty().toSet()
            fun decode(file: String) = assets.open("sprites/$file").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
            return SpriteSheet(
                Sprite.entries.associateWith { sprite ->
                    val extra = generateSequence(1) { it + 1 }
                        .map { "${sprite.fileName}@$it.png" }
                        .takeWhile { it in files }
                    listOf(decode("${sprite.fileName}.png")) + extra.map(::decode)
                },
            )
        }

        private fun frameDurationMs(sprite: Sprite): Long = when (sprite) {
            Sprite.WATER, Sprite.WATER_HOLE, Sprite.WATER_INVISIBLE, Sprite.BOX_WATER, Sprite.BOAT, Sprite.BUOY -> 260
            Sprite.ENERGY, Sprite.LAMP_ACTIVE, Sprite.TELEPORTER, Sprite.ITEM_TELEPORTER -> 140
            Sprite.PUSHY, Sprite.PUSHY_EAST, Sprite.PUSHY_SOUTH, Sprite.PUSHY_WEST -> 260
            else -> 200
        }
    }
}

/**
 * Draws [image] scaled into the cell rectangle at ([left], [top]) of size [width]×[height] as crisp
 * pixel art. Rounds to whole pixels so neighbouring tiles never leave hairline gaps.
 */
fun DrawScope.drawSprite(image: ImageBitmap, left: Float, top: Float, width: Float, height: Float, alpha: Float = 1f) {
    val l = left.roundToInt()
    val t = top.roundToInt()
    drawImage(
        image = image,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(l, t),
        dstSize = IntSize((left + width).roundToInt() - l, (top + height).roundToInt() - t),
        alpha = alpha.coerceIn(0f, 1f),
        filterQuality = FilterQuality.None,
    )
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
                            // Fast drags can skip cells â€“ visit every cell on the line in between.
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
                for (sprite in layer[c]) drawSprite(sprites[sprite], c.col * cellWidth, c.row * cellHeight, cellWidth, cellHeight)
            }
        }
        drawLayer(terrain)
        drawLayer(entities)
    }
}
