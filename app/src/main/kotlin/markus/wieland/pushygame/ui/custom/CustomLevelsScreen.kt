package markus.wieland.pushygame.ui.custom

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import markus.wieland.pushygame.R
import markus.wieland.pushygame.appContainer
import markus.wieland.pushygame.data.LevelEntity
import markus.wieland.pushygame.data.toLevelData
import markus.wieland.pushygame.engine.level.LevelFormatException
import markus.wieland.pushygame.engine.level.ShareCode
import markus.wieland.pushygame.ui.Glyphs
import markus.wieland.pushygame.ui.IslandBackground
import markus.wieland.pushygame.ui.LevelCard
import markus.wieland.pushygame.ui.LevelPreview
import markus.wieland.pushygame.ui.PixelGlyph
import markus.wieland.pushygame.ui.PixelIconButton
import markus.wieland.pushygame.ui.PixelPlank
import markus.wieland.pushygame.ui.ScreenHeader
import markus.wieland.pushygame.ui.SolvedBadge
import markus.wieland.pushygame.ui.pixelTextStyle

@Composable
fun CustomLevelsScreen(onBack: () -> Unit, onPlay: (Long) -> Unit, onEdit: (Long) -> Unit) {
    val context = LocalContext.current
    val repository = context.appContainer.levels
    val levels by repository.customLevels.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var importing by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<LevelEntity?>(null) }

    fun share(level: LevelEntity) {
        val code = ShareCode.encode(level.toLevelData())
        val intent =
            Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, code)
        context.startActivity(Intent.createChooser(intent, level.name))
    }

    IslandBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.custom_title), onBack) {
                HeaderPlank(Glyphs.Import, stringResource(R.string.custom_import)) {
                    importing = true
                }
                Spacer(Modifier.width(10.dp))
                HeaderPlank(Glyphs.Add, stringResource(R.string.custom_new)) { onEdit(0) }
            }
            if (levels.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.custom_empty),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(48.dp),
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(240.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(levels, key = { it.id }) { level ->
                    CustomLevelCard(
                        level = level,
                        onPlay = { onPlay(level.id) },
                        onEdit = { onEdit(level.id) },
                        onShare = { share(level) },
                        onDelete = { deleting = level },
                    )
                }
            }
        }
    }

    if (importing) {
        ImportDialog(
            onDismiss = { importing = false },
            onImport = { code ->
                val level = withContext(Dispatchers.Default) {
                    try {
                        ShareCode.decode(code).takeIf { it.problems().isEmpty() }
                    } catch (_: LevelFormatException) {
                        null
                    }
                } ?: return@ImportDialog false
                repository.saveCustom(null, level)
                importing = false
                true
            },
        )
    }
    deleting?.let { level ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            text = { Text(stringResource(R.string.custom_delete_confirm, level.name)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.delete(level.id) }
                    deleting = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    deleting = null
                }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun CustomLevelCard(
    level: LevelEntity,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    val data = remember(level.json) { level.toLevelData() }
    LevelCard(
        onClick = onPlay,
        preview = {
            LevelPreview(data, Modifier.fillMaxWidth())
            if (level.solved) SolvedBadge(Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp))
        },
        footer = {
            Text(
                level.name,
                style = pixelTextStyle(11.sp, shadow = 3f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp, start = 2.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(if (level.solved) R.string.custom_solved else R.string.custom_unsolved),
                style = pixelTextStyle(
                    8.sp,
                    if (level.solved) Color(0xFF8BE07A) else Color(0xFFF9E7BD),
                    shadow = 2f
                ),
                modifier = Modifier.padding(start = 2.dp),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val button = Modifier.size(40.dp)
                PixelIconButton(Glyphs.Play, stringResource(R.string.custom_play), onPlay, button)
                PixelIconButton(Glyphs.Edit, stringResource(R.string.custom_edit), onEdit, button)
                // Like in the original game, a level can only be shared after its author solved it.
                PixelIconButton(
                    Glyphs.Share,
                    stringResource(if (level.solved) R.string.custom_share else R.string.custom_share_locked),
                    onShare,
                    button,
                    enabled = level.solved,
                )
                Spacer(Modifier.weight(1f))
                PixelIconButton(Glyphs.Delete, stringResource(R.string.delete), onDelete, button)
            }
        },
    )
}

/** Small plank button with a pixel icon and a label, for the header. */
@Composable
private fun HeaderPlank(glyph: List<String>, label: String, onClick: () -> Unit) {
    PixelPlank(onClick, Modifier.height(44.dp), pixel = 2.dp, nails = false) { colors ->
        PixelGlyph(glyph, pixel = 2.dp, color = colors.text)
        Spacer(Modifier.width(8.dp))
        Text(label, style = pixelTextStyle(10.sp, colors.text, shadow = 3f), maxLines = 1)
    }
}

@Composable
private fun ImportDialog(onDismiss: () -> Unit, onImport: suspend (String) -> Boolean) {
    var code by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_import)) },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it
                    invalid = false
                },
                placeholder = { Text(stringResource(R.string.custom_import_hint)) },
                isError = invalid,
                supportingText = { if (invalid) Text(stringResource(R.string.custom_import_invalid)) },
                maxLines = 4,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { scope.launch { invalid = !onImport(code) } },
                enabled = code.isNotBlank()
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
