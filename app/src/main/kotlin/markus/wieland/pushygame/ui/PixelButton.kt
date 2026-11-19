package markus.wieland.pushygame.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.render.LocalSprites

/** Colours of a pixel-art plank, matching the sprite palette (tools/sprites/canvas.py). */
class PlankColors internal constructor(val face: Color, val grain: Color, val highlight: Color, val lip: Color, val text: Color)

internal val Outline = Color(0xFF2B1D2E)
internal val Wood = PlankColors(
    face = Color(0xFFC08A52),
    grain = Color(0xFF9A6438),
    highlight = Color(0xFFE2B37A),
    lip = Color(0xFF62391F),
    text = Color(0xFFFBF7EF),
)
internal val Stone = PlankColors(
    face = Color(0xFF8F909C),
    grain = Color(0xFF7A7B88),
    highlight = Color(0xFFB8BAC4),
    lip = Color(0xFF5F6170),
    text = Color(0xFFD9DAE0),
)

/** Size of one "pixel" of the button art. */
private val Px: Dp = 3.dp
private const val LIP = 4 // pixels of plank edge below the face
private const val PRESS = 3 // pixels the face sinks when pressed

/**
 * Chunky wooden sign button in the style of the game's sprites: notched corners, a thick lower
 * edge that the face sinks into when pressed, and an optional sprite icon.
 */
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Sprite? = null,
    enabled: Boolean = true,
) {
    val sprites = LocalSprites.current
    PixelPlank(onClick, modifier.height(Px * (LIP + 16)), enabled) { colors ->
        if (icon != null) {
            Image(
                bitmap = sprites[icon],
                contentDescription = null,
                filterQuality = FilterQuality.None,
                modifier = Modifier.size(Px * 10).alpha(if (enabled) 1f else 0.6f),
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(text, style = pixelTextStyle(13.sp, colors.text), maxLines = 1, softWrap = false)
    }
}

/** Square plank button showing a [PixelGlyph] – for the D-pad, back, restart … */
@Composable
fun PixelIconButton(
    glyph: List<String>,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pixel: Dp = 2.dp,
    enabled: Boolean = true,
) {
    PixelPlank(
        onClick,
        modifier.semantics { this.contentDescription = contentDescription },
        enabled = enabled,
        pixel = pixel,
        nails = false,
        sidePadding = 0.dp,
    ) { colors -> PixelGlyph(glyph, pixel = pixel, color = colors.text) }
}

/** Shared plank button body; [content] is laid out centred on the plank face. */
@Composable
fun PixelPlank(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pixel: Dp = Px,
    nails: Boolean = true,
    sidePadding: Dp = pixel * 6,
    content: @Composable RowScope.(PlankColors) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink = if (pressed && enabled) PRESS else 0
    val colors = if (enabled) Wood else Stone
    Row(
        modifier
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .drawBehind { drawPlank(colors, sink, pixel.toPx(), nails) }
            .offset(y = pixel * sink)
            .padding(bottom = pixel * LIP, start = sidePadding, end = sidePadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) { content(colors) }
}

fun pixelTextStyle(size: TextUnit, color: Color = Color(0xFFFBF7EF), shadow: Float = 6f) = TextStyle(
    fontFamily = PixelFont,
    fontSize = size,
    lineHeight = size * 1.5f,
    color = color,
    shadow = Shadow(Outline, offset = Offset(0f, shadow), blurRadius = 0f),
)

/**
 * Tiny pixel icon from ASCII art ('#' = filled), drawn white with a dark outline and drop shadow.
 */
@Composable
fun PixelGlyph(rows: List<String>, modifier: Modifier = Modifier, pixel: Dp = 2.dp, color: Color = Color(0xFFFBF7EF)) {
    val w = rows.maxOf { it.length }
    val h = rows.size
    Canvas(modifier.size(pixel * (w + 2), pixel * (h + 3))) {
        val p = pixel.toPx()
        fun filled(x: Int, y: Int) = y in 0 until h && x in 0 until rows[y].length && rows[y][x] == '#'
        // outline + shadow first
        for (y in -1..h + 1) for (x in -1..w) {
            val near = (-1..1).any { dy -> (-1..1).any { dx -> filled(x + dx, y + dy) || filled(x + dx, y + dy - 1) } }
            if (near && !filled(x, y)) drawRect(Outline, Offset((x + 1) * p, (y + 1) * p), Size(p, p))
        }
        for (y in 0 until h) for (x in 0 until w) {
            if (filled(x, y)) drawRect(color, Offset((x + 1) * p, (y + 1) * p), Size(p, p))
        }
    }
}

object Glyphs {
    val Up = listOf("...#...", "..###..", ".#####.", "#######", "..###..", "..###..", "..###..")
    val Down = Up.reversed()
    val Left = Up.transposeLeft()
    val Right = Left.map { it.reversed() }
    val Restart = listOf(
        "..####.",
        ".#...##",
        "#...###",
        "#......",
        "#.....#",
        ".#...#.",
        "..###..",
    )
    val Back = listOf("..#....", ".##....", "#######", ".##....", "..#....")
    val Play = listOf("#....", "##...", "###..", "####.", "###..", "##...", "#....")
    val Edit = listOf(".....##", "....###", "...###.", "..###..", ".###...", "##.....", "#......")
    val Share = listOf("...#...", "..###..", ".#.#.#.", "...#...", "#..#..#", "#.....#", "#######")
    val Delete = listOf("..###..", "#######", ".#####.", ".#.#.#.", ".#.#.#.", ".#.#.#.", ".#####.")
    val Lock = listOf(".###.", "#...#", "#...#", "#####", "##.##", "##.##", "#####")
    val Check = listOf("......#", ".....##", "#...##.", "##.##..", ".###...", "..#....")
    val Import = listOf("...#...", "...#...", ".#.#.#.", "..###..", "#..#..#", "#.....#", "#######")
    val Add = listOf("..#..", "..#..", "#####", "..#..", "..#..")
}

/** Rotates ASCII art a quarter turn counter-clockwise (an up arrow becomes a left arrow). */
private fun List<String>.transposeLeft(): List<String> {
    val w = maxOf { it.length }
    return (w - 1 downTo 0).map { x -> joinToString("") { row -> row.getOrElse(x) { '.' }.toString() } }
}

/** Wooden notice board behind the side panel. */
fun Modifier.pixelPanel(pixel: Dp = Px, seams: Boolean = true): Modifier = drawBehind {
    val p = pixel.toPx()
    val w = size.width
    val h = size.height
    fun notched(left: Float, top: Float, right: Float, bottom: Float, color: Color) {
        drawRect(color, Offset(left + p, top), Size(right - left - 2 * p, bottom - top))
        drawRect(color, Offset(left, top + p), Size(right - left, bottom - top - 2 * p))
    }
    notched(0f, 0f, w, h, Outline)
    notched(p, p, w - p, h - p, Wood.lip)
    notched(p, p, w - p, h - 3 * p, Wood.face)
    // horizontal planks
    val plank = 34 * p
    var y = p + plank
    while (seams && y < h - 6 * p) {
        drawRect(Wood.lip, Offset(p, y), Size(w - 2 * p, p))
        drawRect(Wood.highlight, Offset(2 * p, y + p), Size(w - 4 * p, p))
        y += plank
    }
    drawRect(Wood.highlight, Offset(2 * p, 2 * p), Size(w - 4 * p, p))
}

/** Recessed slot (inventory items) carved into the panel. */
fun Modifier.pixelSlot(pixel: Dp = 2.dp): Modifier = drawBehind {
    val p = pixel.toPx()
    val w = size.width
    val h = size.height
    drawRect(Wood.lip, Offset(p, 0f), Size(w - 2 * p, h))
    drawRect(Wood.lip, Offset(0f, p), Size(w, h - 2 * p))
    drawRect(Color(0xFF3A2416), Offset(p, p), Size(w - 2 * p, 2 * p))
    drawRect(Color(0xFF7A4A28), Offset(2 * p, 3 * p), Size(w - 4 * p, h - 5 * p))
    drawRect(Wood.grain, Offset(2 * p, h - 2 * p), Size(w - 4 * p, p))
}

private fun DrawScope.drawPlank(colors: PlankColors, sink: Int, p: Float, nails: Boolean) {
    val w = size.width
    val h = size.height

    /** Rectangle with its four corner pixels cut off. */
    fun notched(left: Float, top: Float, right: Float, bottom: Float, color: Color) {
        drawRect(color, Offset(left + p, top), Size(right - left - 2 * p, bottom - top))
        drawRect(color, Offset(left, top + p), Size(right - left, bottom - top - 2 * p))
    }

    // Outline and the dark lower edge of the plank.
    notched(0f, sink * p, w, h, Outline)
    notched(p, (sink + 1) * p, w - p, h - p, colors.lip)

    // The face, raised by the remaining lip.
    val faceTop = (sink + 1) * p
    val faceBottom = h - p - (LIP - sink) * p
    notched(p, faceTop, w - p, faceBottom, colors.face)
    drawRect(Outline, Offset(2 * p, faceBottom), Size(w - 4 * p, p))

    // Highlight along the top edge, darker grain lines across the plank.
    drawRect(colors.highlight, Offset(3 * p, faceTop + p), Size(w - 6 * p, p))
    // Grain only towards the ends, so it never runs through the label.
    val faceHeight = faceBottom - faceTop
    fun grain(x: Float, y: Float, length: Float) {
        drawRect(colors.grain, Offset(w * x, faceTop + (faceHeight * y / p).toInt() * p), Size(w * length, p))
    }
    grain(0.05f, 0.72f, 0.1f)
    grain(0.08f, 0.3f, 0.05f)
    grain(0.84f, 0.38f, 0.1f)
    grain(0.88f, 0.75f, 0.05f)

    // Nails at both ends.
    if (!nails) return
    val nailY = faceTop + ((faceHeight / 2) / p).toInt() * p - p
    for (x in listOf(3 * p, w - 5 * p)) {
        drawRect(Outline, Offset(x, nailY), Size(2 * p, 2 * p))
        drawRect(colors.highlight, Offset(x, nailY), Size(p, p))
    }
}
