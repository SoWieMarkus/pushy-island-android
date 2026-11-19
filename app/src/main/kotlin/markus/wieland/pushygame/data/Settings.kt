package markus.wieland.pushygame.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

/** Which screen edge the in-game panel (controls and inventory) sits on. */
enum class PanelSide { LEFT, RIGHT }

/** Small player preferences, persisted in [android.content.SharedPreferences]. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _panelSide = MutableStateFlow(
        prefs.getString(KEY_PANEL_SIDE, null)
            ?.let { name -> PanelSide.entries.firstOrNull { it.name == name } }
            ?: PanelSide.RIGHT,
    )
    val panelSide: StateFlow<PanelSide> = _panelSide.asStateFlow()

    fun setPanelSide(side: PanelSide) {
        _panelSide.value = side
        prefs.edit { putString(KEY_PANEL_SIDE, side.name) }
    }

    private companion object {
        const val KEY_PANEL_SIDE = "panel_side"
    }
}
