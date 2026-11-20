package markus.wieland.pushygame.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import markus.wieland.pushygame.R
import markus.wieland.pushygame.appContainer
import markus.wieland.pushygame.data.PanelSide
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.ui.IslandBackground
import markus.wieland.pushygame.ui.PixelButton
import markus.wieland.pushygame.ui.ScreenHeader
import markus.wieland.pushygame.ui.pixelPanel
import markus.wieland.pushygame.ui.pixelTextStyle

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val settings = LocalContext.current.appContainer.settings
    val panelSide by settings.panelSide.collectAsStateWithLifecycle()
    val soundEnabled by settings.soundEnabled.collectAsStateWithLifecycle()

    IslandBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.settings_title), onBack)
            Column(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .pixelPanel()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.settings_panel_side),
                    style = pixelTextStyle(11.sp, shadow = 4f),
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SideOption(
                        stringResource(R.string.settings_left),
                        selected = panelSide == PanelSide.LEFT,
                        onClick = { settings.setPanelSide(PanelSide.LEFT) },
                    )
                    SideOption(
                        stringResource(R.string.settings_right),
                        selected = panelSide == PanelSide.RIGHT,
                        onClick = { settings.setPanelSide(PanelSide.RIGHT) },
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.settings_sound),
                    style = pixelTextStyle(11.sp, shadow = 4f),
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SideOption(
                        stringResource(R.string.settings_on),
                        selected = soundEnabled,
                        onClick = { settings.setSoundEnabled(true) },
                    )
                    SideOption(
                        stringResource(R.string.settings_off),
                        selected = !soundEnabled,
                        onClick = { settings.setSoundEnabled(false) },
                    )
                }
            }
        }
    }
}

/** Pushy stands on the selected option. */
@Composable
private fun SideOption(text: String, selected: Boolean, onClick: () -> Unit) {
    PixelButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.width(190.dp),
        icon = if (selected) Sprite.PUSHY_SOUTH else null,
    )
}
