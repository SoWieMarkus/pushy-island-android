package markus.wieland.pushygame.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import markus.wieland.pushygame.R
import markus.wieland.pushygame.appContainer
import markus.wieland.pushygame.engine.editor.EditMode
import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.level.LevelProblem
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.model.TileType
import markus.wieland.pushygame.engine.previewSprites
import markus.wieland.pushygame.render.BoardCanvas
import markus.wieland.pushygame.render.LocalSprites
import markus.wieland.pushygame.ui.PushyColors

@Composable
fun EditorScreen(levelId: Long, onDone: () -> Unit) {
    val repository = LocalContext.current.appContainer.levels
    val viewModel = viewModel { EditorViewModel(levelId, repository) }
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val sprites = LocalSprites.current
    var renaming by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.saved) { if (ui.saved) onDone() }
    BackHandler(enabled = ui.hasChanges) { confirmDiscard = true }

    val problemText = ui.problem?.let { problemMessage(it) }
    LaunchedEffect(problemText) {
        if (problemText != null) {
            snackbar.showSnackbar(problemText)
            viewModel.dismissProblem()
        }
    }

    Box(Modifier.fillMaxSize().background(PushyColors.DeepOcean)) {
        Column(Modifier.fillMaxSize()) {
            Toolbar(
                ui = ui,
                onBack = { if (ui.hasChanges) confirmDiscard = true else onDone() },
                onRename = { renaming = true },
                viewModel = viewModel,
            )
            Row(Modifier.weight(1f)) {
                Box(Modifier.weight(1f).fillMaxHeight().padding(8.dp), contentAlignment = Alignment.Center) {
                    val (terrain, entities) = remember(ui.level) { ui.level.previewSprites() }
                    BoardCanvas(sprites, terrain, entities, onCellPressed = viewModel::press)
                }
                Palette(ui.selected, viewModel::select, Modifier.width(232.dp).fillMaxHeight())
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    if (renaming) {
        RenameDialog(
            initial = ui.level.name,
            onDismiss = { renaming = false },
            onConfirm = {
                viewModel.rename(it)
                renaming = false
            },
        )
    }
    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            text = { Text(stringResource(R.string.editor_discard_changes)) },
            confirmButton = { TextButton(onClick = onDone) { Text(stringResource(R.string.discard)) } },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun problemMessage(problem: LevelProblem): String = when (problem) {
    LevelProblem.MissingPushy -> stringResource(R.string.editor_missing_pushy)
    LevelProblem.MissingFinish -> stringResource(R.string.editor_missing_finish)
    LevelProblem.InvalidName -> stringResource(R.string.editor_invalid_name)
    is LevelProblem.TooManyInstances -> stringResource(R.string.editor_too_many, problem.tileId, problem.max)
}

@Composable
private fun Toolbar(ui: EditorUi, onBack: () -> Unit, onRename: () -> Unit, viewModel: EditorViewModel) {
    Row(
        Modifier.background(PushyColors.Panel).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
        Text(
            ui.level.name.ifBlank { stringResource(R.string.editor_untitled) },
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).clickable(onClick = onRename).padding(8.dp),
        )
        IconButton(onClick = onRename) { Icon(Icons.Filled.DriveFileRenameOutline, stringResource(R.string.editor_rename)) }

        @Composable
        fun ModeButton(mode: EditMode, icon: ImageVector, label: Int) {
            IconToggleButton(checked = ui.mode == mode, onCheckedChange = { viewModel.setMode(mode) }) {
                Icon(icon, stringResource(label), tint = if (ui.mode == mode) PushyColors.Sand else Color.White)
            }
        }
        ModeButton(EditMode.PENCIL, Icons.Filled.Edit, R.string.editor_pencil)
        ModeButton(EditMode.FILL, Icons.Filled.FormatColorFill, R.string.editor_fill)
        ModeButton(EditMode.ERASE, Icons.Outlined.Backspace, R.string.editor_erase)
        IconButton(onClick = viewModel::undo, enabled = ui.canUndo) {
            Icon(Icons.AutoMirrored.Filled.Undo, stringResource(R.string.editor_undo))
        }
        IconButton(onClick = viewModel::redo, enabled = ui.canRedo) {
            Icon(Icons.AutoMirrored.Filled.Redo, stringResource(R.string.editor_redo))
        }
        IconButton(onClick = viewModel::smooth) { Icon(Icons.Filled.AutoFixHigh, stringResource(R.string.editor_smooth)) }
        IconButton(onClick = viewModel::reset) { Icon(Icons.Filled.DeleteSweep, stringResource(R.string.editor_reset)) }
        IconButton(onClick = viewModel::save) {
            Icon(Icons.Filled.Check, stringResource(R.string.save), tint = PushyColors.Palm)
        }
    }
}

@Composable
private fun Palette(selected: TileType, onSelect: (TileType) -> Unit, modifier: Modifier = Modifier) {
    val sprites = LocalSprites.current
    val tiles: List<TileType> = TerrainType.entries + EntityType.entries
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier.background(PushyColors.Panel).padding(6.dp),
    ) {
        items(tiles, key = { "${it.layer}-${it.id}" }) { tile ->
            val isSelected = tile == selected
            Box(
                Modifier
                    .padding(2.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (tile.layer == TileType.Layer.ENTITY) PushyColors.Sand.copy(alpha = 0.35f) else Color.Transparent)
                    .border(if (isSelected) 3.dp else 0.dp, if (isSelected) PushyColors.Coral else Color.Transparent, RoundedCornerShape(4.dp))
                    .clickable { onSelect(tile) },
            ) {
                Image(
                    bitmap = sprites[tile.sprite],
                    contentDescription = tile.id,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.fillMaxSize().padding(3.dp),
                )
            }
        }
    }
}

@Composable
private fun RenameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    val valid = name.isNotBlank() && name.trim().length <= LevelData.MAX_NAME_LENGTH
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.editor_rename)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(LevelData.MAX_NAME_LENGTH) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                supportingText = { Text("${name.length} / ${LevelData.MAX_NAME_LENGTH}") },
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }, enabled = valid) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
