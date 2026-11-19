package markus.wieland.pushygame.engine.board

import markus.wieland.pushygame.engine.entity.Entity
import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Field
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.terrain.Terrain

/**
 * Mutable game board: a terrain layer that is always filled and an entity layer with empty cells.
 * Fields know their own coordinate; the board keeps it in sync on every write.
 */
class Board(private val terrainGrid: Grid<Terrain>, private val entityGrid: Grid<Entity?>) {

    val height get() = terrainGrid.height
    val width get() = terrainGrid.width

    fun isInside(c: Coordinate) = terrainGrid.isInside(c)

    fun entityAt(c: Coordinate): Entity? = if (isInside(c)) entityGrid[c] else null
    fun entityAt(field: Field): Entity? = entityAt(field.coordinate)

    fun terrainAt(c: Coordinate): Terrain? = if (isInside(c)) terrainGrid[c] else null

    /** Terrain at a coordinate that is known to be on the board. */
    fun terrain(c: Coordinate): Terrain = terrainGrid[c]
    fun terrain(field: Field): Terrain = terrainGrid[field.coordinate]

    fun setEntity(c: Coordinate, entity: Entity?) {
        if (!isInside(c)) return
        entityGrid[c] = entity
        entity?.coordinate = c
    }

    fun setTerrain(c: Coordinate, terrain: Terrain) {
        if (!isInside(c)) return
        terrainGrid[c] = terrain
        terrain.coordinate = c
    }

    fun setTerrain(c: Coordinate, type: TerrainType) = setTerrain(c, Terrain.create(type, c))

    fun removeEntity(entity: Entity) {
        if (isInside(entity.coordinate)) entityGrid[entity.coordinate] = null
    }

    fun swapEntities(from: Coordinate, to: Coordinate) {
        val a = entityAt(from)
        val b = entityAt(to)
        setEntity(from, b)
        setEntity(to, a)
    }

    fun swapTerrain(from: Coordinate, to: Coordinate) {
        val a = terrainAt(from) ?: return
        val b = terrainAt(to) ?: return
        setTerrain(from, b)
        setTerrain(to, a)
    }

    val entities: List<Entity> get() = entityGrid.filterNotNull()
    val terrains: List<Terrain> get() = terrainGrid.toList()

    inline fun <reified T> entitiesOf(): List<T> = entities.filterIsInstance<T>()
    inline fun <reified T> terrainsOf(): List<T> = terrains.filterIsInstance<T>()

    fun terrainSnapshot(): Grid<Terrain> = terrainGrid.copy()
    fun entitySnapshot(): Grid<Entity?> = entityGrid.copy()

    companion object {
        fun from(level: LevelData) = Board(
            terrainGrid = level.terrain.map { c, type -> Terrain.create(type, c) },
            entityGrid = level.entities.map { c, type -> type?.let { Entity.create(it, c) } },
        )
    }
}
