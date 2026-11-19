package markus.wieland.pushygame.ui.game

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import markus.wieland.pushygame.data.LevelRepository
import markus.wieland.pushygame.data.toLevelData
import markus.wieland.pushygame.engine.Game
import markus.wieland.pushygame.engine.GameState
import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.model.Direction
import kotlin.time.Duration.Companion.milliseconds

data class GameUi(
    val name: String = "",
    val campaignNumber: Int? = null,
    val state: GameState? = null,
    /** Set once the level is solved; null if there is no next level. */
    val nextLevelId: Long? = null,
)

class GameViewModel(
    private val levelId: Long,
    private val isTest: Boolean,
    private val repository: LevelRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(GameUi())
    val ui: StateFlow<GameUi> = _ui.asStateFlow()

    private var level: LevelData? = null
    private var game: Game? = null
    private var gameJob: Job? = null

    init {
        viewModelScope.launch {
            val entity = repository.get(levelId) ?: return@launch
            level = entity.toLevelData()
            _ui.update { it.copy(name = entity.name, campaignNumber = entity.campaignNumber) }
            restart()
        }
    }

    fun move(direction: Direction) {
        game?.move(direction)
    }

    fun restart() {
        val level = level ?: return
        gameJob?.cancel()
        val game = Game(level).also { game = it }
        _ui.update { it.copy(state = game.state.value, nextLevelId = null) }
        gameJob = viewModelScope.launch {
            launch { game.state.collect { state -> onState(state) } }
            // Drive the game clock (logic gates, buttons, …) with real time.
            var last = SystemClock.uptimeMillis()
            while (isActive) {
                delay(FRAME_MS.milliseconds)
                val now = SystemClock.uptimeMillis()
                game.advance(now - last)
                last = now
            }
        }
    }

    private suspend fun onState(state: GameState) {
        val wasFinished = _ui.value.state?.isFinished == true
        _ui.update { it.copy(state = state) }
        if (state.isFinished && !wasFinished) {
            repository.markSolved(levelId)
            val next = if (isTest) null else repository.nextCampaignLevel(levelId)?.id
            _ui.update { it.copy(nextLevelId = next) }
        }
    }

    private companion object {
        const val FRAME_MS = 16L
    }
}
