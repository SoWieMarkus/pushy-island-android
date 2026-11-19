package markus.wieland.pushygame.engine.level

import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.TerrainType

/** A level as authored: one terrain tile and at most one entity per cell. */
data class LevelData(
    val name: String,
    val terrain: Grid<TerrainType>,
    val entities: Grid<EntityType?>,
) {
    init {
        require(terrain.height == entities.height && terrain.width == entities.width) {
            "Terrain and entity layers must have the same size"
        }
    }

    val height get() = terrain.height
    val width get() = terrain.width

    /** Problems that prevent the level from being played. Empty means the level is playable. */
    fun problems(): List<LevelProblem> = buildList {
        val counts = entities.filterNotNull().groupingBy { it }.eachCount()
        val pushies = counts[EntityType.PUSHY] ?: 0
        if (pushies == 0) add(LevelProblem.MissingPushy)
        if (counts[EntityType.FINISH] == null) add(LevelProblem.MissingFinish)
        if (name.isBlank() || name.length > MAX_NAME_LENGTH) add(LevelProblem.InvalidName)
        val terrainCounts = terrain.groupingBy { it }.eachCount()
        (counts + terrainCounts).forEach { (type, count) ->
            val max = type.maxInstances
            if (max != null && count > max) add(LevelProblem.TooManyInstances(type.id, max))
        }
    }

    companion object {
        const val DEFAULT_HEIGHT = 12
        const val DEFAULT_WIDTH = 20
        const val MAX_NAME_LENGTH = 20

        fun empty(name: String = "", height: Int = DEFAULT_HEIGHT, width: Int = DEFAULT_WIDTH) = LevelData(
            name = name,
            terrain = Grid(height, width) { TerrainType.WATER },
            entities = Grid(height, width) { null },
        )
    }
}

sealed interface LevelProblem {
    data object MissingPushy : LevelProblem
    data object MissingFinish : LevelProblem
    data object InvalidName : LevelProblem
    data class TooManyInstances(val tileId: String, val max: Int) : LevelProblem
}
