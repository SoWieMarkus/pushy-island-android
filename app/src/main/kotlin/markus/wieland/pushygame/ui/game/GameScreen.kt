package markus.wieland.pushygame.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import markus.wieland.pushygame.R
import markus.wieland.pushygame.appContainer
import markus.wieland.pushygame.engine.InventoryItem
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.render.AnimatedBoard
import markus.wieland.pushygame.render.LocalSprites
import markus.wieland.pushygame.ui.Glyphs
import markus.wieland.pushygame.ui.PixelButton
import markus.wieland.pushygame.ui.PixelIconButton
import markus.wieland.pushygame.ui.PushyColors
import markus.wieland.pushygame.ui.pixelPanel
import markus.wieland.pushygame.ui.pixelSlot
import markus.wieland.pushygame.ui.pixelTextStyle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GameScreen(levelId: Long, isTest: Boolean, onBack: () -> Unit, onNextLevel: (Long) -> Unit) {
    val repository = LocalContext.current.appContainer.levels
    val viewModel = viewModel { GameViewModel(levelId, isTest, repository) }
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val sprites = LocalSprites.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Box(
        Modifier
            .fillMaxSize()
            .background(PushyColors.DeepOcean)
            .focusRequester(focus)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val direction = when (event.key) {
                    Key.DirectionUp, Key.W -> Direction.NORTH
                    Key.DirectionDown, Key.S -> Direction.SOUTH
                    Key.DirectionLeft, Key.A -> Direction.WEST
                    Key.DirectionRight, Key.D -> Direction.EAST
                    Key.R -> {
                        viewModel.restart()
                        return@onKeyEvent true
                    }

                    else -> return@onKeyEvent false
                }
                viewModel.move(direction)
                true
            },
    ) {
        // The sea fills the whole screen; the board is centred left of the panel.
        ui.state?.let { state ->
            AnimatedBoard(
                state,
                sprites,
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectSwipes(viewModel::move) },
                endInset = PanelWidth + PanelMargin * 2,
            )
        }
        SidePanel(
            ui = ui,
            onBack = onBack,
            onRestart = viewModel::restart,
            onMove = viewModel::move,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(PanelMargin)
                .width(PanelWidth)
                .fillMaxHeight(),
        )
    }

    // Let the winning move finish animating before the dialog covers the board.
    val finished = ui.state?.isFinished == true
    var showSolved by remember { mutableStateOf(false) }
    LaunchedEffect(finished) {
        if (finished) delay(SOLVED_DELAY_MS.milliseconds)
        showSolved = finished
    }
    if (showSolved) {
        SolvedDialog(
            name = ui.name,
            moves = ui.state?.moves ?: 0,
            nextLevelId = ui.nextLevelId,
            onBack = onBack,
            onNextLevel = onNextLevel,
        )
    }
}

private const val SOLVED_DELAY_MS = 450L

/** "Solved!" notice board: Pushy bounces for joy, plank buttons to go on. */
@Composable
private fun SolvedDialog(
    name: String,
    moves: Int,
    nextLevelId: Long?,
    onBack: () -> Unit,
    onNextLevel: (Long) -> Unit
) {
    val sprites = LocalSprites.current
    val pop = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 500f)) }
    val bounce = rememberInfiniteTransition(label = "bounce")
    val hop by bounce.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(560, easing = LinearEasing)),
        label = "hop",
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            Modifier
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                }
                .width(460.dp)
                .pixelPanel(seams = false)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Image(
                    sprites[Sprite.COIN],
                    null,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(10.dp))
                Image(
                    sprites[Sprite.PUSHY_SOUTH],
                    null,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer {
                            val air = sin(PI.toFloat() * hop)
                            translationY = -air * 14.dp.toPx()
                            // squash a little when landing
                            val squash = if (hop !in 0.12f..0.88f) 0.1f else 0f
                            scaleX = 1f + squash
                            scaleY = 1f - squash
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        },
                )
                Spacer(Modifier.width(10.dp))
                Image(
                    sprites[Sprite.COIN],
                    null,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.game_solved_title),
                style = pixelTextStyle(20.sp, PushyColors.Sand, shadow = 6f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                pluralStringResource(R.plurals.game_solved_text, moves, name, moves),
                style = pixelTextStyle(10.sp, shadow = 3f).copy(textAlign = TextAlign.Center),
            )
            Spacer(Modifier.height(20.dp))
            if (nextLevelId != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PixelButton(stringResource(R.string.back), onBack, Modifier.weight(1f))
                    PixelButton(
                        stringResource(R.string.game_next),
                        { onNextLevel(nextLevelId) },
                        Modifier.weight(1.6f),
                        icon = Sprite.FINISH
                    )
                }
            } else {
                PixelButton(
                    stringResource(R.string.ok),
                    onBack,
                    Modifier.fillMaxWidth(),
                    icon = Sprite.FINISH
                )
            }
        }
    }
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectSwipes(onSwipe: (Direction) -> Unit) {
    var dx = 0f
    var dy = 0f
    val threshold = 32.dp.toPx()
    detectDragGestures(
        onDragStart = {
            dx = 0f
            dy = 0f
        },
        onDrag = { change, amount ->
            change.consume()
            dx += amount.x
            dy += amount.y
            // Allow walking several steps in one gesture.
            if (abs(dx) > threshold || abs(dy) > threshold) {
                onSwipe(
                    if (abs(dx) > abs(dy)) {
                        if (dx > 0) Direction.EAST else Direction.WEST
                    } else {
                        if (dy > 0) Direction.SOUTH else Direction.NORTH
                    },
                )
                dx = 0f
                dy = 0f
            }
        },
    )
}

private val PanelWidth = 188.dp
private val PanelMargin = 10.dp

@Composable
private fun SidePanel(
    ui: GameUi,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onMove: (Direction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier
        .pixelPanel()
        .padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PixelIconButton(
                Glyphs.Back,
                stringResource(R.string.back),
                onBack,
                Modifier.size(44.dp)
            )
            Spacer(Modifier.weight(1f))
            PixelIconButton(
                Glyphs.Restart,
                stringResource(R.string.game_restart),
                onRestart,
                Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        ui.campaignNumber?.let {
            Text("%03d".format(it), style = pixelTextStyle(11.sp, PushyColors.Sand, shadow = 4f))
            Spacer(Modifier.height(4.dp))
        }
        Text(
            ui.name,
            style = pixelTextStyle(12.sp, shadow = 4f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            (ui.state?.moves ?: 0).let { pluralStringResource(R.plurals.game_moves, it, it) },
            style = pixelTextStyle(9.sp, Color(0xFFF9E7BD), shadow = 3f),
        )
        Spacer(Modifier.height(12.dp))
        Inventory(ui.state?.inventory.orEmpty(), Modifier.weight(1f))
        DPad(onMove, Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun Inventory(items: List<InventoryItem>, modifier: Modifier = Modifier) {
    val sprites = LocalSprites.current
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for ((sprite, count) in items) {
            Box(Modifier
                .size(46.dp)
                .pixelSlot()) {
                Image(
                    bitmap = sprites[sprite],
                    contentDescription = null,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                )
                Text(
                    "$count",
                    style = pixelTextStyle(9.sp, shadow = 3f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 4.dp, bottom = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun DPad(onMove: (Direction) -> Unit, modifier: Modifier = Modifier) {
    @Composable
    fun Pad(direction: Direction, glyph: List<String>, label: Int) {
        PixelIconButton(
            glyph,
            stringResource(label),
            { onMove(direction) },
            Modifier.size(50.dp),
            pixel = 3.dp
        )
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Pad(Direction.NORTH, Glyphs.Up, R.string.game_up)
        Row(horizontalArrangement = Arrangement.spacedBy(50.dp)) {
            Pad(Direction.WEST, Glyphs.Left, R.string.game_left)
            Pad(Direction.EAST, Glyphs.Right, R.string.game_right)
        }
        Pad(Direction.SOUTH, Glyphs.Down, R.string.game_down)
    }
}
