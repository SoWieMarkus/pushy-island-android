package markus.wieland.pushygame.engine.editor

import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.model.TileType
import markus.wieland.pushygame.engine.terrain.Grass
import markus.wieland.pushygame.engine.terrain.Sand
import markus.wieland.pushygame.engine.terrain.Terrain
import markus.wieland.pushygame.engine.terrain.Water

enum class EditMode { PENCIL, FILL, ERASE }

/**
 * Editing operations on a level with undo/redo. Every operation produces a new immutable
 * [LevelData]; the history keeps the last [HISTORY_LIMIT] states.
 */
class LevelEditor(initial: LevelData) {

    var level: LevelData = initial
        private set

    private val undoStack = ArrayDeque<LevelData>()
    private val redoStack = ArrayDeque<LevelData>()

    val canUndo get() = undoStack.isNotEmpty()
    val canRedo get() = redoStack.isNotEmpty()

    /** Applies [tile] at [c] the way the editor's [mode] says. */
    fun apply(c: Coordinate, tile: TileType, mode: EditMode) = when (mode) {
        EditMode.ERASE -> erase(c)
        EditMode.FILL -> if (tile.maxInstances != null || tile.isLogic) place(c, tile) else fill(c, tile)
        EditMode.PENCIL -> place(c, tile)
    }

    fun place(c: Coordinate, tile: TileType) = edit { terrain, entities ->
        when {
            tile == EntityType.PIRATE || tile == EntityType.PIRATE_HUT -> placePirate(c, tile, terrain, entities)
            tile.maxInstances != null -> placeLimited(c, tile, terrain, entities)
            else -> put(c, tile, terrain, entities)
        }
    }

    /** Removes the entity at [c], or turns the terrain back into water if there is none. */
    fun erase(c: Coordinate) = edit { terrain, entities ->
        if (entities[c] != null) entities[c] = null else terrain[c] = TerrainType.WATER
    }

    /** Flood-fills the connected area of equal tiles (on [tile]'s layer) starting at [c]. */
    fun fill(c: Coordinate, tile: TileType) = edit { terrain, entities ->
        val read: (Coordinate) -> TileType? = when (tile) {
            is TerrainType -> { p -> terrain[p] }
            is EntityType -> { p -> entities[p] }
        }
        val replaced = read(c)
        if (replaced == tile) return@edit
        val queue = ArrayDeque(listOf(c))
        val seen = mutableSetOf(c)
        while (queue.isNotEmpty()) {
            val p = queue.removeFirst()
            put(p, tile, terrain, entities)
            for (d in Direction.entries) {
                val n = p.next(d)
                if (terrain.isInside(n) && n !in seen && read(n) == replaced) {
                    seen += n
                    queue += n
                }
            }
        }
    }

    /** Picks matching corner tiles for every plain sand and grass field. */
    fun smooth() = edit { terrain, _ ->
        val classes = terrain.map { c, type -> Terrain.create(type, c) }
        fun at(c: Coordinate) = if (classes.isInside(c)) classes[c] else null
        fun sandLike(t: Terrain?) = t == null || t is Sand || t is Water
        fun land(t: Terrain?) = t is Grass || t is Sand

        for (c in terrain.coordinates) {
            val type = terrain[c]
            val top = at(c.next(Direction.NORTH))
            val left = at(c.next(Direction.WEST))
            val right = at(c.next(Direction.EAST))
            val down = at(c.next(Direction.SOUTH))
            terrain[c] = when (type) {
                in SAND_TILES -> when {
                    !land(down) && !land(right) && land(left) && land(top) -> TerrainType.SAND_BOTTOM_RIGHT
                    !land(down) && !land(left) && land(right) && land(top) -> TerrainType.SAND_BOTTOM_LEFT
                    !land(top) && !land(right) && land(left) && land(down) -> TerrainType.SAND_TOP_RIGHT
                    !land(top) && !land(left) && land(right) && land(down) -> TerrainType.SAND_TOP_LEFT
                    else -> TerrainType.SAND
                }
                in GRASS_TILES -> when {
                    !sandLike(down) && !sandLike(right) && sandLike(left) && sandLike(top) -> TerrainType.GRASS_TOP_LEFT
                    !sandLike(down) && !sandLike(left) && sandLike(right) && sandLike(top) -> TerrainType.GRASS_TOP_RIGHT
                    !sandLike(top) && !sandLike(right) && sandLike(left) && sandLike(down) -> TerrainType.GRASS_BOTTOM_LEFT
                    !sandLike(top) && !sandLike(left) && sandLike(right) && sandLike(down) -> TerrainType.GRASS_BOTTOM_RIGHT
                    else -> TerrainType.GRASS
                }
                else -> type
            }
        }
    }

    fun reset() = edit { terrain, entities ->
        for (c in terrain.coordinates) {
            terrain[c] = TerrainType.WATER
            entities[c] = null
        }
    }

    fun rename(name: String) {
        if (name == level.name) return
        commit(level.copy(name = name))
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(level)
        level = previous
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(level)
        level = next
    }

    private fun edit(block: (Grid<TerrainType>, Grid<EntityType?>) -> Unit) {
        val terrain = level.terrain.copy()
        val entities = level.entities.copy()
        block(terrain, entities)
        commit(level.copy(terrain = terrain, entities = entities))
    }

    private fun commit(next: LevelData) {
        if (next == level) return
        undoStack.addLast(level)
        if (undoStack.size > HISTORY_LIMIT) undoStack.removeFirst()
        redoStack.clear()
        level = next
    }

    private fun put(c: Coordinate, tile: TileType, terrain: Grid<TerrainType>, entities: Grid<EntityType?>) {
        if (!terrain.isInside(c)) return
        when (tile) {
            is TerrainType -> terrain[c] = tile
            is EntityType -> entities[c] = tile
        }
    }

    /** Keeps at most `maxInstances - 1` existing copies, then places the new one. */
    private fun placeLimited(c: Coordinate, tile: TileType, terrain: Grid<TerrainType>, entities: Grid<EntityType?>) {
        var keep = (tile.maxInstances ?: Int.MAX_VALUE) - 1
        for (p in terrain.coordinates) {
            val current = if (tile is TerrainType) terrain[p] else entities[p]
            if (current != tile) continue
            if (keep > 0) {
                keep--
                continue
            }
            if (tile is TerrainType) terrain[p] = TerrainType.SAND else entities[p] = null
        }
        put(c, tile, terrain, entities)
    }

    /** The pirate always stands directly north of his hut; placing one places both. */
    private fun placePirate(c: Coordinate, tile: TileType, terrain: Grid<TerrainType>, entities: Grid<EntityType?>) {
        var pirate: Coordinate
        var hut: Coordinate
        if (tile == EntityType.PIRATE) {
            pirate = c
            hut = c.next(Direction.SOUTH)
            if (!terrain.isInside(hut)) {
                hut = c
                pirate = c.next(Direction.NORTH)
            }
        } else {
            hut = c
            pirate = c.next(Direction.NORTH)
            if (!terrain.isInside(pirate)) {
                pirate = c
                hut = c.next(Direction.SOUTH)
            }
        }
        placeLimited(pirate, EntityType.PIRATE, terrain, entities)
        placeLimited(hut, EntityType.PIRATE_HUT, terrain, entities)
    }

    companion object {
        const val HISTORY_LIMIT = 100

        private val SAND_TILES = setOf(
            TerrainType.SAND, TerrainType.SAND_TOP_LEFT, TerrainType.SAND_TOP_RIGHT,
            TerrainType.SAND_BOTTOM_LEFT, TerrainType.SAND_BOTTOM_RIGHT,
        )
        private val GRASS_TILES = setOf(
            TerrainType.GRASS, TerrainType.GRASS_TOP_LEFT, TerrainType.GRASS_TOP_RIGHT,
            TerrainType.GRASS_BOTTOM_LEFT, TerrainType.GRASS_BOTTOM_RIGHT,
        )
    }
}
