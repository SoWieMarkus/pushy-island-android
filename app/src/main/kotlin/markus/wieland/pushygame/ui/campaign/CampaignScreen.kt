package markus.wieland.pushygame.ui.campaign

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import markus.wieland.pushygame.R
import markus.wieland.pushygame.appContainer
import markus.wieland.pushygame.data.LevelEntity
import markus.wieland.pushygame.data.toLevelData
import markus.wieland.pushygame.ui.IslandBackground
import markus.wieland.pushygame.ui.LevelPreview
import markus.wieland.pushygame.ui.PushyColors
import markus.wieland.pushygame.ui.ScreenHeader

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
                    style = MaterialTheme.typography.labelLarge,
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
    Card(
        onClick = onClick,
        enabled = !locked,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        ),
        border = if (level.solved) BorderStroke(2.dp, PushyColors.Palm) else null,
    ) {
        Box {
            LevelPreview(data, Modifier.fillMaxWidth(), dimmed = locked)
            if (locked) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.campaign_locked),
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            if (level.solved) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = PushyColors.Palm,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                )
            }
        }
        Text(
            "%03d  %s".format(level.campaignNumber, level.name),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        )
    }
}
