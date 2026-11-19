package markus.wieland.pushygame.engine

import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.TerrainType
import java.io.File

val repoRoot: File = File(System.getProperty("repoRoot") ?: "..")

/**
 * Builds a small level from ASCII art. Terrain: `-` sand, `~` water, `=` grass, others via [terrain].
 * Entities: `.` empty, `P` Pushy, others via [entities].
 */
fun level(
    terrainRows: String,
    entityRows: String,
    terrain: Map<Char, TerrainType> = emptyMap(),
    entities: Map<Char, EntityType> = emptyMap(),
): LevelData {
    val t = terrainRows.trimIndent().lines()
    val e = entityRows.trimIndent().lines()
    val terrainMap = mapOf('-' to TerrainType.SAND, '~' to TerrainType.WATER, '=' to TerrainType.GRASS) + terrain
    val entityMap = mapOf('P' to EntityType.PUSHY, 'F' to EntityType.FINISH, 'B' to EntityType.BOX, 'T' to EntityType.TREE) + entities
    return LevelData(
        name = "Test",
        terrain = Grid(t.size, t[0].length) { terrainMap.getValue(t[it.row][it.col]) },
        entities = Grid(e.size, e[0].length) { c -> e[c.row][c.col].let { if (it == '.') null else entityMap.getValue(it) } },
    )
}

/** Entity layer of the game as ASCII, using the same symbols as [level] (unknown entities become `?`). */
fun Game.entityRows(symbols: Map<EntityType, Char> = emptyMap()): String {
    val map = mapOf(EntityType.PUSHY to 'P', EntityType.FINISH to 'F', EntityType.BOX to 'B', EntityType.TREE to 'T') + symbols
    return (0 until board.height).joinToString("\n") { row ->
        (0 until board.width).joinToString("") { col ->
            val entity = board.entityAt(markus.wieland.pushygame.engine.model.Coordinate(row, col))
            if (entity == null) "." else (map[entity.type] ?: '?').toString()
        }
    }
}
