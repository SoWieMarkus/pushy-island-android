package markus.wieland.pushygame.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import markus.wieland.pushygame.ui.Glyphs
import markus.wieland.pushygame.ui.PixelDialog
import markus.wieland.pushygame.ui.PixelDialogText
import markus.wieland.pushygame.ui.PixelIconButton
import markus.wieland.pushygame.ui.PixelTextField
import markus.wieland.pushygame.ui.PushyColors
import markus.wieland.pushygame.ui.pixelFrame
import markus.wieland.pushygame.ui.pixelPanel
import markus.wieland.pushygame.ui.pixelSlot
import markus.wieland.pushygame.ui.pixelTextStyle

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
                Palette(
                    ui.selected,
                    viewModel::select,
                    Modifier.padding(top = 8.dp, end = 8.dp, bottom = 8.dp).width(232.dp).fillMaxHeight(),
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) { data ->
            PixelDialogText(
                data.visuals.message,
                Modifier.pixelPanel(seams = false).padding(horizontal = 18.dp, vertical = 14.dp),
            )
        }
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
        PixelDialog(
            onDismissRequest = { confirmDiscard = false },
            confirmText = stringResource(R.string.discard),
            onConfirm = onDone,
            dismissText = stringResource(R.string.cancel),
        ) { PixelDialogText(stringResource(R.string.editor_discard_changes)) }
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
        Modifier
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .pixelPanel(seams = false)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        @Composable
        fun Tool(glyph: List<String>, label: Int, onClick: () -> Unit, enabled: Boolean = true, selected: Boolean = false) {
            PixelIconButton(glyph, stringResource(label), onClick, ToolButton, enabled = enabled, selected = selected)
        }

        @Composable
        fun Gap() = Spacer(Modifier.width(12.dp))

        Tool(Glyphs.Back, R.string.back, onBack)
        Gap()
        Text(
            ui.level.name.ifBlank { stringResource(R.string.editor_untitled) },
            style = pixelTextStyle(12.sp, shadow = 4f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).clickable(onClick = onRename).padding(vertical = 8.dp),
        )
        Tool(Glyphs.Rename, R.string.editor_rename, onRename)
        Gap()
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Tool(Glyphs.Edit, R.string.editor_pencil, { viewModel.setMode(EditMode.PENCIL) }, selected = ui.mode == EditMode.PENCIL)
            Tool(Glyphs.Fill, R.string.editor_fill, { viewModel.setMode(EditMode.FILL) }, selected = ui.mode == EditMode.FILL)
            Tool(Glyphs.Erase, R.string.editor_erase, { viewModel.setMode(EditMode.ERASE) }, selected = ui.mode == EditMode.ERASE)
        }
        Gap()
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Tool(Glyphs.Undo, R.string.editor_undo, viewModel::undo, enabled = ui.canUndo)
            Tool(Glyphs.Redo, R.string.editor_redo, viewModel::redo, enabled = ui.canRedo)
            Tool(Glyphs.Smooth, R.string.editor_smooth, viewModel::smooth)
            Tool(Glyphs.Delete, R.string.editor_reset, viewModel::reset)
        }
        Gap()
        Tool(Glyphs.Check, R.string.save, viewModel::save)
    }
}

private val ToolButton = Modifier.size(40.dp)

@Composable
private fun Palette(selected: TileType, onSelect: (TileType) -> Unit, modifier: Modifier = Modifier) {
    val sprites = LocalSprites.current
    val sections = listOf<Pair<Int, List<TileType>>>(
        R.string.editor_terrain to TerrainType.entries,
        R.string.editor_objects to EntityType.entries,
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier.pixelPanel(seams = false).padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for ((title, tiles) in sections) {
            item(key = title, span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    stringResource(title),
                    style = pixelTextStyle(9.sp, PushyColors.Sand, shadow = 3f),
                    modifier = Modifier.padding(start = 2.dp, top = 4.dp, bottom = 2.dp),
                )
            }
            items(tiles, key = { "${it.layer}-${it.id}" }) { tile ->
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .pixelSlot()
                        .then(if (tile == selected) Modifier.pixelFrame(PushyColors.Sand) else Modifier)
                        .clickable(role = Role.Button) { onSelect(tile) },
                ) {
                    Image(
                        bitmap = sprites[tile.sprite],
                        contentDescription = tile.id,
                        filterQuality = FilterQuality.None,
                        modifier = Modifier.fillMaxSize().padding(5.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RenameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    val valid = name.isNotBlank() && name.trim().length <= LevelData.MAX_NAME_LENGTH
    PixelDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.editor_rename),
        confirmText = stringResource(R.string.ok),
        onConfirm = { onConfirm(name) },
        confirmEnabled = valid,
        dismissText = stringResource(R.string.cancel),
    ) {
        PixelTextField(
            value = name,
            onValueChange = { name = it.take(LevelData.MAX_NAME_LENGTH) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (valid) onConfirm(name) }),
            supportingText = "${name.length} / ${LevelData.MAX_NAME_LENGTH}",
        )
    }
}
