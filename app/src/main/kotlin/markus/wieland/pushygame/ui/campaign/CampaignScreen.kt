package markus.wieland.pushygame.ui.campaign

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import markus.wieland.pushygame.R
import markus.wieland.pushygame.appContainer
import markus.wieland.pushygame.data.LevelEntity
import markus.wieland.pushygame.data.toLevelData
import markus.wieland.pushygame.ui.Glyphs
import markus.wieland.pushygame.ui.IslandBackground
import markus.wieland.pushygame.ui.LevelCard
import markus.wieland.pushygame.ui.LevelPreview
import markus.wieland.pushygame.ui.PixelGlyph
import markus.wieland.pushygame.ui.PushyColors
import markus.wieland.pushygame.ui.ScreenHeader
import markus.wieland.pushygame.ui.SolvedBadge
import markus.wieland.pushygame.ui.pixelTextStyle

@Composable
fun CampaignScreen(onBack: () -> Unit, onPlay: (Long) -> Unit) {
    val levels by LocalContext.current.appContainer.levels.campaign.collectAsState(initial = emptyList())
    // Levels unlock one after another: everything up to the first unsolved level is playable.
    val firstUnsolved = levels.indexOfFirst { !it.solved }.let { if (it == -1) levels.size else it }
    val gridState = rememberLazyGridState()
    LaunchedEffect(levels.isNotEmpty()) {
        if (levels.isNotEmpty()) gridState.scrollToItem((firstUnsolved - 2).coerceAtLeast(0))
    }

    IslandBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.campaign_title), onBack) {
                Text(
                    stringResource(R.string.campaign_progress, levels.count { it.solved }, levels.size),
                    style = pixelTextStyle(11.sp, shadow = 4f),
                    modifier = Modifier.padding(end = 16.dp),
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(180.dp),
                state = gridState,
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            ) {
                items(levels, key = { it.id }) { level ->
                    val index = level.campaignNumber!! - 1
                    CampaignCard(level, locked = index > firstUnsolved, onClick = { onPlay(level.id) })
                }
            }
        }
    }
}

@Composable
private fun CampaignCard(level: LevelEntity, locked: Boolean, onClick: () -> Unit) {
    val data = remember(level.json) { level.toLevelData() }
    val lockedLabel = stringResource(R.string.campaign_locked)
    LevelCard(
        onClick = onClick,
        enabled = !locked,
        preview = {
            LevelPreview(data, Modifier.fillMaxWidth(), dimmed = locked)
            if (locked) {
                PixelGlyph(
                    Glyphs.Lock,
                    Modifier
                        .align(Alignment.Center)
                        .semantics { contentDescription = lockedLabel },
                    pixel = 4.dp,
                )
            }
            if (level.solved) SolvedBadge(Modifier.align(Alignment.TopEnd).padding(6.dp))
        },
        footer = {
            Row(Modifier.padding(top = 8.dp, start = 2.dp, end = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("%03d".format(level.campaignNumber), style = pixelTextStyle(10.sp, Color(0xFFFFE066), shadow = 3f))
                Spacer(Modifier.width(8.dp))
                Text(
                    level.name,
                    style = pixelTextStyle(10.sp, shadow = 3f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    )
}
