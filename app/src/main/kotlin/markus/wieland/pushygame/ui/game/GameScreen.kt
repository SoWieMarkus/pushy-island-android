package markus.wieland.pushygame.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import markus.wieland.pushygame.R
import markus.wieland.pushygame.appContainer
import markus.wieland.pushygame.engine.InventoryItem
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.render.BoardCanvas
import markus.wieland.pushygame.render.LocalSprites
import markus.wieland.pushygame.ui.PushyColors
import kotlin.math.abs

@Composable
fun GameScreen(levelId: Long, isTest: Boolean, onBack: () -> Unit, onNextLevel: (Long) -> Unit) {
    val repository = LocalContext.current.appContainer.levels
    val viewModel = viewModel { GameViewModel(levelId, isTest, repository) }
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val sprites = LocalSprites.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Row(
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
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) { detectSwipes(viewModel::move) },
            contentAlignment = Alignment.Center,
        ) {
            ui.state?.let { state ->
                BoardCanvas(sprites, state.terrain, state.entities)
            }
        }
        SidePanel(
            ui = ui,
            onBack = onBack,
            onRestart = viewModel::restart,
            onMove = viewModel::move,
        )
    }

    if (ui.state?.isFinished == true) {
        val next = ui.nextLevelId
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.game_solved_title)) },
            text = { Text((ui.state?.moves ?: 0).let { pluralStringResource(R.plurals.game_solved_text, it, ui.name, it) }) },
            confirmButton = {
                if (next != null) {
                    TextButton(onClick = { onNextLevel(next) }) { Text(stringResource(R.string.game_next)) }
                } else {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.ok)) }
                }
            },
            dismissButton = {
                if (next != null) TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
            },
        )
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

@Composable
private fun SidePanel(ui: GameUi, onBack: () -> Unit, onRestart: () -> Unit, onMove: (Direction) -> Unit) {
    Column(
        Modifier
            .width(184.dp)
            .fillMaxHeight()
            .background(PushyColors.Panel)
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onRestart) {
                Icon(Icons.Filled.Refresh, stringResource(R.string.game_restart))
            }
        }
        ui.campaignNumber?.let {
            Text("%03d".format(it), style = MaterialTheme.typography.labelLarge, color = PushyColors.Sand)
        }
        Text(
            ui.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text((ui.state?.moves ?: 0).let { pluralStringResource(R.plurals.game_moves, it, it) }, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Inventory(ui.state?.inventory.orEmpty(), Modifier.weight(1f))
        DPad(onMove, Modifier.align(Alignment.CenterHorizontally))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Inventory(items: List<InventoryItem>, modifier: Modifier = Modifier) {
    val sprites = LocalSprites.current
    FlowRow(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (item in items) {
            Row(
                Modifier
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    bitmap = sprites[item.sprite],
                    contentDescription = null,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(28.dp),
                )
                Text("×${item.count}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
private fun DPad(onMove: (Direction) -> Unit, modifier: Modifier = Modifier) {
    @Composable
    fun Pad(direction: Direction, icon: ImageVector, label: Int) {
        FilledTonalIconButton(onClick = { onMove(direction) }, modifier = Modifier.size(52.dp)) {
            Icon(icon, stringResource(label))
        }
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Pad(Direction.NORTH, Icons.Filled.KeyboardArrowUp, R.string.game_up)
        Row(horizontalArrangement = Arrangement.spacedBy(52.dp)) {
            Pad(Direction.WEST, Icons.Filled.KeyboardArrowLeft, R.string.game_left)
            Pad(Direction.EAST, Icons.Filled.KeyboardArrowRight, R.string.game_right)
        }
        Pad(Direction.SOUTH, Icons.Filled.KeyboardArrowDown, R.string.game_down)
    }
}
