package markus.wieland.pushygame.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import markus.wieland.pushygame.BuildConfig
import markus.wieland.pushygame.R
import markus.wieland.pushygame.appContainer
import markus.wieland.pushygame.ui.IslandBackground

@Composable
fun MenuScreen(onCampaign: () -> Unit, onCustomLevels: () -> Unit) {
    val repository = LocalContext.current.appContainer.levels
    val campaign by repository.campaign.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    // As in the original game, the editor unlocks after the first campaign level.
    val customUnlocked = campaign.firstOrNull()?.solved == true

    IslandBackground {
        Column(
            Modifier.align(Alignment.Center).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall.copy(
                    shadow = Shadow(Color(0xAA000000), blurRadius = 8f),
                ),
                color = Color.White,
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = onCampaign, modifier = Modifier.width(280.dp)) {
                Text(stringResource(R.string.menu_campaign), style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onCustomLevels, enabled = customUnlocked, modifier = Modifier.width(280.dp)) {
                Text(stringResource(R.string.menu_custom_levels), style = MaterialTheme.typography.labelLarge)
            }
            if (!customUnlocked) {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.menu_custom_levels_locked), color = Color.White)
            }
            if (BuildConfig.DEBUG) {
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { scope.launch { repository.markCampaignSolved() } }) {
                    Text(stringResource(R.string.menu_debug_solve_all), color = Color.White)
                }
            }
        }
    }
}
