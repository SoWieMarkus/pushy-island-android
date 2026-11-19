package markus.wieland.pushygame.render

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
class SpriteSheet(
    private val frames: Map<Sprite, List<ImageBitmap>>,
    private val edges: Map<String, List<ImageBitmap>> = emptyMap(),
) {

    /** First frame – used for icons, the editor and previews. */
    operator fun get(sprite: Sprite): ImageBitmap = frames.getValue(sprite)[0]

    /** The frame of [sprite] to show at [timeMs]. */
    fun frame(sprite: Sprite, timeMs: Long): ImageBitmap {
        val list = frames.getValue(sprite)
        if (list.size == 1) return list[0]
        return list[((timeMs / frameDurationMs(sprite)) % list.size).toInt()]
    }

    val hasAnimations: Boolean = frames.values.any { it.size > 1 }

    /** A terrain transition overlay from `sprites/edges` (see [TerrainEdges]), or null if there is none. */
    fun edge(name: String, timeMs: Long): ImageBitmap? {
        val list = edges[name] ?: return null
        return list[((timeMs / EDGE_FRAME_MS) % list.size).toInt()]
    }

    companion object {
        fun load(assets: AssetManager): SpriteSheet {
            fun folder(path: String): Pair<Set<String>, (String) -> List<ImageBitmap>> {
                val files = assets.list(path).orEmpty().toSet()
                fun decode(file: String) = assets.open("$path/$file").use { BitmapFactory.decodeStream(it) }.asImageBitmap()
                return files to { name ->
                    val extra = generateSequence(1) { it + 1 }.map { "$name@$it.png" }.takeWhile { it in files }
                    listOf(decode("$name.png")) + extra.map(::decode)
                }
            }
            val (_, sprite) = folder("sprites")
            val (edgeFiles, edge) = folder("sprites/edges")
            return SpriteSheet(
                frames = Sprite.entries.associateWith { sprite(it.fileName) },
                edges = edgeFiles.filter { '@' !in it }.map { it.removeSuffix(".png") }.associateWith(edge),
            )
        }

        private const val EDGE_FRAME_MS = 420L

        private fun frameDurationMs(sprite: Sprite): Long = when (sprite) {
            Sprite.WATER, Sprite.WATER_HOLE, Sprite.WATER_INVISIBLE, Sprite.BOX_WATER, Sprite.BOAT, Sprite.BUOY -> 260
            Sprite.ENERGY, Sprite.LAMP_ACTIVE, Sprite.TELEPORTER, Sprite.ITEM_TELEPORTER -> 140
            Sprite.PUSHY, Sprite.PUSHY_EAST, Sprite.PUSHY_SOUTH, Sprite.PUSHY_WEST -> 260
            else -> 200
        }
    }
}

/** Sprites are drawn for a 16×16 cell; bigger ones have a transparent overhang around that cell. */
const val TILE_PIXELS = 16

/**
 * Draws [image] scaled into the cell rectangle at ([left], [top]) of size [width]×[height] as crisp
 * pixel art. Rounds to whole pixels so neighbouring tiles never leave hairline gaps.
 *
 * Sprites larger than [TILE_PIXELS] (a crab's claws, a palm's crown, a flag) stay centred on the
 * cell at the same pixel scale and spill over into the neighbouring cells.
 */
fun DrawScope.drawSprite(image: ImageBitmap, left: Float, top: Float, width: Float, height: Float, alpha: Float = 1f) {
    if (image.width > TILE_PIXELS) {
        val overhangX = (image.width - TILE_PIXELS) / 2f * width / TILE_PIXELS
        val overhangY = (image.height - TILE_PIXELS) / 2f * height / TILE_PIXELS
        return drawSpriteExactly(image, left - overhangX, top - overhangY, width + 2 * overhangX, height + 2 * overhangY, alpha)
    }
    drawSpriteExactly(image, left, top, width, height, alpha)
}

private fun DrawScope.drawSpriteExactly(image: ImageBitmap, left: Float, top: Float, width: Float, height: Float, alpha: Float) {
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
    val edges = remember { TerrainEdgesCache() }
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
        fun drawLayer(layer: Grid<List<Sprite>>, map: (List<Sprite>) -> List<Sprite> = { it }) {
            for (c in layer.coordinates) {
                for (sprite in map(layer[c])) drawSprite(sprites[sprite], c.col * cellWidth, c.row * cellHeight, cellWidth, cellHeight)
            }
        }
        drawLayer(terrain, TerrainEdges::displayed)
        edges.of(terrain).draw(this, sprites, 0L, Size(cellWidth, cellHeight))
        drawLayer(entities)
    }
}
