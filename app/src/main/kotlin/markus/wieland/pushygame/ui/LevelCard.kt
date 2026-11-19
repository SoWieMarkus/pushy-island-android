package markus.wieland.pushygame.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CardPx: Dp = 2.dp
private const val CARD_LIP = 3
private const val CARD_PRESS = 2
private const val FRAME = 4 // wooden frame around the preview, in pixels

private val Grass = Color(0xFF4FB35A)
private val GrassDark = Color(0xFF2F7A3D)

/**
 * A level as a little framed picture: the [preview] sits in a wooden frame, [footer] below it
 * on the same plank. Locked cards ([enabled] = false) turn to stone. The card sinks when pressed.
 */
@Composable
fun LevelCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    preview: @Composable BoxScope.() -> Unit,
    footer: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink = if (pressed && enabled) CARD_PRESS else 0
    val colors = if (enabled) Wood else Stone
    Column(
        modifier
            .clickable(interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .drawBehind { drawCardFrame(colors, sink) }
            .offset(y = CardPx * sink)
            .padding(start = CardPx * FRAME, end = CardPx * FRAME, top = CardPx * FRAME, bottom = CardPx * (CARD_LIP + 3)),
    ) {
        // The picture, with a dark inner edge so it looks set into the frame.
        Box(
            Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    val p = CardPx.toPx()
                    drawRect(Outline, Offset.Zero, Size(size.width, p))
                    drawRect(Outline, Offset.Zero, Size(p, size.height))
                    drawRect(Outline, Offset(size.width - p, 0f), Size(p, size.height))
                    drawRect(Outline, Offset(0f, size.height - p), Size(size.width, p))
                    drawRect(Color.Black.copy(alpha = 0.25f), Offset(p, p), Size(size.width - 2 * p, p))
                },
            content = preview,
        )
        footer()
    }
}

private fun DrawScope.drawCardFrame(colors: PlankColors, sink: Int) {
    val p = CardPx.toPx()
    val w = size.width
    val h = size.height
    fun notched(left: Float, top: Float, right: Float, bottom: Float, color: Color) {
        drawRect(color, Offset(left + p, top), Size(right - left - 2 * p, bottom - top))
        drawRect(color, Offset(left, top + p), Size(right - left, bottom - top - 2 * p))
    }
    notched(0f, sink * p, w, h, Outline)
    notched(p, (sink + 1) * p, w - p, h - p, colors.lip)
    val faceBottom = h - p - (CARD_LIP - sink) * p
    notched(p, (sink + 1) * p, w - p, faceBottom, colors.face)
    drawRect(Outline, Offset(2 * p, faceBottom), Size(w - 4 * p, p))
    drawRect(colors.highlight, Offset(3 * p, (sink + 2) * p), Size(w - 6 * p, p))
    // nails in the frame corners
    for ((x, y) in listOf(2f to 2f, w / p - 3f to 2f)) {
        drawRect(Outline, Offset(x * p, (y + sink) * p), Size(p, p))
    }
}

/** Green "solved" stamp with a pixel check mark. */
@Composable
fun SolvedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(28.dp)
            .drawBehind {
                val p = 2.dp.toPx()
                val w = size.width
                val h = size.height
                drawRect(Outline, Offset(p, 0f), Size(w - 2 * p, h))
                drawRect(Outline, Offset(0f, p), Size(w, h - 2 * p))
                drawRect(GrassDark, Offset(p, p), Size(w - 2 * p, h - 2 * p))
                drawRect(Grass, Offset(p, p), Size(w - 2 * p, h - 4 * p))
            },
        contentAlignment = Alignment.Center,
    ) { PixelGlyph(Glyphs.Check, pixel = 2.dp) }
}
