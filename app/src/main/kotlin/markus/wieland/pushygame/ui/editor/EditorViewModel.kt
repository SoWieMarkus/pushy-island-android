package markus.wieland.pushygame.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import markus.wieland.pushygame.data.LevelRepository
import markus.wieland.pushygame.data.toLevelData
import markus.wieland.pushygame.engine.editor.EditMode
import markus.wieland.pushygame.engine.editor.LevelEditor
import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.level.LevelProblem
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.model.TileType

data class EditorUi(
    val level: LevelData = LevelData.empty(),
    val selected: TileType = TerrainType.SAND,
    val mode: EditMode = EditMode.PENCIL,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val hasChanges: Boolean = false,
    val problem: LevelProblem? = null,
    val saved: Boolean = false,
)

class EditorViewModel(private val levelId: Long, private val repository: LevelRepository) : ViewModel() {

    private var editor = LevelEditor(LevelData.empty())
    private var savedLevel = editor.level

    private val _ui = MutableStateFlow(EditorUi())
    val ui: StateFlow<EditorUi> = _ui.asStateFlow()

    init {
        if (levelId != 0L) {
            viewModelScope.launch {
                val level = repository.get(levelId)?.toLevelData() ?: return@launch
                editor = LevelEditor(level)
                savedLevel = level
                publish()
            }
        }
    }

    fun select(tile: TileType) = _ui.update {
        it.copy(selected = tile, mode = if (it.mode == EditMode.ERASE) EditMode.PENCIL else it.mode)
    }

    fun setMode(mode: EditMode) = _ui.update { it.copy(mode = mode) }

    fun press(c: Coordinate, isStrokeStart: Boolean) {
        val ui = _ui.value
        // Filling while dragging would flood the board over and over – only fill on the first cell.
        if (ui.mode == EditMode.FILL && !isStrokeStart) return
        editor.apply(c, ui.selected, ui.mode, continueStroke = !isStrokeStart)
        publish()
    }

    fun undo() = edit { undo() }
    fun redo() = edit { redo() }
    fun smooth() = edit { smooth() }
    fun reset() = edit { reset() }
    fun rename(name: String) = edit { rename(name.trim().take(LevelData.MAX_NAME_LENGTH)) }

    fun dismissProblem() = _ui.update { it.copy(problem = null) }

    fun save() {
        val problem = editor.level.problems().firstOrNull()
        if (problem != null) {
            _ui.update { it.copy(problem = problem) }
            return
        }
        viewModelScope.launch {
            repository.saveCustom(levelId.takeIf { it != 0L }, editor.level)
            savedLevel = editor.level
            _ui.update { it.copy(saved = true, hasChanges = false) }
        }
    }

    private inline fun edit(block: LevelEditor.() -> Unit) {
        editor.block()
        publish()
    }

    private fun publish() = _ui.update {
        it.copy(
            level = editor.level,
            canUndo = editor.canUndo,
            canRedo = editor.canRedo,
            hasChanges = editor.level != savedLevel,
        )
    }
}
