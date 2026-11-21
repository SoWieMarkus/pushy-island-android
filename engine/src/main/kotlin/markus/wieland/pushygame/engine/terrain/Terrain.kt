package markus.wieland.pushygame.engine.terrain

import markus.wieland.pushygame.engine.Game
import markus.wieland.pushygame.engine.board.Board
import markus.wieland.pushygame.engine.entity.Coconut
import markus.wieland.pushygame.engine.entity.Entity
import markus.wieland.pushygame.engine.entity.Pushy
import markus.wieland.pushygame.engine.logic.LogicInput
import markus.wieland.pushygame.engine.logic.LogicOutput
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Field
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.engine.model.TerrainType

abstract class Terrain(var type: TerrainType, coordinate: Coordinate) : Field(coordinate) {

    override val tileType: TerrainType get() = type

    open val elevation: Int get() = ELEVATION_SAND

    override fun destroysFlyingStone() = elevation > ELEVATION_SAND

    fun hasSameElevationAs(other: Terrain) = elevation == other.elevation

    open val coconutCanEnterFromTunnel: Boolean get() = false
    open val isCoconutTunnel: Boolean get() = false

    companion object {
        const val ELEVATION_WATER = -1
        const val ELEVATION_SAND = 0
        const val ELEVATION_GRASS = 1
        const val ELEVATION_BLOCKED = Int.MAX_VALUE

        fun create(type: TerrainType, c: Coordinate): Terrain = when (type) {
            TerrainType.SAND, TerrainType.SAND_TOP_LEFT, TerrainType.SAND_TOP_RIGHT,
            TerrainType.SAND_BOTTOM_LEFT, TerrainType.SAND_BOTTOM_RIGHT -> Sand(type, c)
            TerrainType.GRASS, TerrainType.GRASS_TOP_LEFT, TerrainType.GRASS_TOP_RIGHT,
            TerrainType.GRASS_BOTTOM_LEFT, TerrainType.GRASS_BOTTOM_RIGHT -> Grass(type, c)
            TerrainType.WATER -> Water(type, c)
            TerrainType.BOX_WATER -> BoxWater(type, c)
            TerrainType.SPRING -> Spring(type, c)
            TerrainType.WATER_HOLE -> WaterHole(type, c)
            TerrainType.FARM -> Farm(type, c)
            TerrainType.STATUE_FINISH_RED, TerrainType.STATUE_FINISH_GREEN,
            TerrainType.STATUE_FINISH_BLUE -> StatueFinish(type, c)
            TerrainType.BOMB_FIELD -> BombField(type, c)
            TerrainType.HOLE -> Hole(type, c)
            TerrainType.SPIKE_PRESSURE_PLATE -> SpikePressurePlate(type, c)
            TerrainType.SPIKES -> Spikes(type, c)
            TerrainType.BOAT -> Boat(type, c)
            TerrainType.TELEPORTER -> Teleporter(type, c)
            TerrainType.ITEM_TELEPORTER -> ItemTeleporter(type, c)
            TerrainType.WATER_INVISIBLE_PRESSURE_PLATE -> InvisibleWaterPressurePlate(type, c)
            TerrainType.WATER_INVISIBLE -> InvisibleWater(type, c)
            TerrainType.COCONUT_TUNNEL -> CoconutTunnel(type, c)
            TerrainType.COCONUT_TUNNEL_FINISH -> CoconutTunnelFinish(type, c)
            TerrainType.ICE -> Ice(type, c)
            TerrainType.FLOWER_RED_PRESSURE_PLATE, TerrainType.FLOWER_YELLOW_PRESSURE_PLATE -> FlowerFinish(type, c)
            TerrainType.STRING_PRESSURE_PLATE -> StringPressurePlate(type, c)
            TerrainType.CHANGEABLE_FLOWER_RED, TerrainType.CHANGEABLE_FLOWER_GREEN -> ChangeableFlower(type, c)
            TerrainType.BARREL_FINISH -> BarrelFinish(type, c)
            TerrainType.CABLE -> Cable(type, c)
            TerrainType.LOGIC_PRESSURE_PLATE -> LogicPressurePlate(type, c)
            TerrainType.DOOR -> Door(type, c)
        }
    }
}

open class Sand(type: TerrainType, c: Coordinate) : Terrain(type, c)

open class Grass(type: TerrainType, c: Coordinate) : Terrain(type, c) {
    override val elevation get() = ELEVATION_GRASS
}

class Water(type: TerrainType, c: Coordinate) : Terrain(type, c) {
    override val elevation get() = ELEVATION_WATER
}

class BoxWater(type: TerrainType, c: Coordinate) : Terrain(type, c)

/** Points its bow where it last sailed (north until then). The hull sprite is drawn over water. */
class Boat(type: TerrainType, c: Coordinate) : Terrain(type, c) {
    var heading = Direction.NORTH

    override val sprites
        get() = listOf(
            Sprite.WATER,
            when (heading) {
                Direction.NORTH -> Sprite.BOAT
                Direction.EAST -> Sprite.BOAT_EAST
                Direction.SOUTH -> Sprite.BOAT_SOUTH
                Direction.WEST -> Sprite.BOAT_WEST
            },
        )

    companion object {
        val SPRITES = setOf(Sprite.BOAT, Sprite.BOAT_EAST, Sprite.BOAT_SOUTH, Sprite.BOAT_WEST)
    }
}

class WaterHole(type: TerrainType, c: Coordinate) : Sand(type, c)

class BarrelFinish(type: TerrainType, c: Coordinate) : Sand(type, c)

/** Looks like water while [showAsWater] is set, but can always be walked on. */
class InvisibleWater(type: TerrainType, c: Coordinate) : Terrain(type, c) {
    var showAsWater = false
    override val sprites get() = listOf(if (showAsWater) Sprite.WATER else Sprite.WATER_INVISIBLE)
    override val coconutCanEnterFromTunnel get() = true
}

class Farm(type: TerrainType, c: Coordinate) : Sand(type, c) {
    var hasSeed = false
    override val sprites get() = if (hasSeed) listOf(Sprite.FARM, Sprite.SEED) else listOf(Sprite.FARM)
}

class Hole(type: TerrainType, c: Coordinate) : Sand(type, c) {
    var isFilled = false
        private set

    fun fill() {
        isFilled = true
    }

    override val elevation get() = if (isFilled) ELEVATION_SAND else 1000
    override val sprites get() = listOf(if (isFilled) Sprite.HOLE_FILLED else Sprite.HOLE)
}

class Spikes(type: TerrainType, c: Coordinate) : Sand(type, c) {
    var isActive = true
        private set

    /** Spikes only come out when nothing is standing on them. */
    fun setActive(active: Boolean, entityOnTop: Entity?) {
        isActive = entityOnTop == null && active
    }

    override val elevation get() = if (isActive) ELEVATION_BLOCKED else ELEVATION_SAND
    override fun destroysFlyingStone() = isActive
    override val sprites get() = listOf(if (isActive) Sprite.SPIKES else Sprite.SPIKES_DEACTIVATED)
}

class StatueFinish(type: TerrainType, c: Coordinate) : Sand(type, c) {
    fun accepts(statue: EntityType?) = when (type) {
        TerrainType.STATUE_FINISH_RED -> statue == EntityType.STATUE_RED
        TerrainType.STATUE_FINISH_GREEN -> statue == EntityType.STATUE_GREEN
        TerrainType.STATUE_FINISH_BLUE -> statue == EntityType.STATUE_BLUE
        else -> false
    }
}

class FlowerFinish(type: TerrainType, c: Coordinate) : Grass(type, c) {
    val flowerType: EntityType
        get() = if (type == TerrainType.FLOWER_RED_PRESSURE_PLATE) EntityType.FLOWER_RED else EntityType.FLOWER_YELLOW
    val oppositeFlowerType: EntityType
        get() = if (type == TerrainType.FLOWER_RED_PRESSURE_PLATE) EntityType.FLOWER_YELLOW else EntityType.FLOWER_RED
}

class ChangeableFlower(type: TerrainType, c: Coordinate) : Sand(type, c) {
    fun toggle() {
        type = if (type == TerrainType.CHANGEABLE_FLOWER_GREEN) TerrainType.CHANGEABLE_FLOWER_RED
        else TerrainType.CHANGEABLE_FLOWER_GREEN
    }
}

class CoconutTunnel(type: TerrainType, c: Coordinate) : Sand(type, c) {
    override val isCoconutTunnel get() = true
    override val coconutCanEnterFromTunnel get() = true
}

class Door(type: TerrainType, c: Coordinate) : Sand(type, c), LogicInput {
    var isOpen = false
        private set

    override val elevation get() = if (isOpen) ELEVATION_SAND else ELEVATION_BLOCKED
    override val sprites get() = listOf(if (isOpen) Sprite.SAND else Sprite.DOOR)

    override fun isInput(direction: Direction) = true

    override fun update(game: Game) {
        isOpen = Direction.entries.any { LogicInput.isInputActive(game, it, coordinate) }
    }
}

class Cable(type: TerrainType, c: Coordinate) : Sand(type, c) {
    var isActive = false
    private var directions: List<Direction> = Direction.entries

    /** Recomputes which arms of the cable are drawn, based on its neighbors. */
    fun updateConnections(board: Board) {
        directions = Direction.entries.filter { connectsTo(board, it) }.ifEmpty { Direction.entries }
    }

    private fun connectsTo(board: Board, direction: Direction): Boolean {
        val next = coordinate.next(direction)
        if (!board.isInside(next)) return false
        val entity = board.entityAt(next)
        val terrain = board.terrainAt(next)
        return terrain is Cable ||
            (terrain is LogicOutput && terrain.isOutput(direction)) ||
            (entity is LogicOutput && entity.isOutput(direction)) ||
            (entity is LogicInput && entity.isInput(direction)) ||
            (terrain is LogicInput && terrain.isInput(direction))
    }

    override val sprites: List<Sprite>
        get() = listOf(Sprite.SAND) + directions.map { direction ->
            when (direction) {
                Direction.NORTH -> if (isActive) Sprite.CABLE_NORTH_ACTIVE else Sprite.CABLE_NORTH
                Direction.EAST -> if (isActive) Sprite.CABLE_EAST_ACTIVE else Sprite.CABLE_EAST
                Direction.SOUTH -> if (isActive) Sprite.CABLE_SOUTH_ACTIVE else Sprite.CABLE_SOUTH
                Direction.WEST -> if (isActive) Sprite.CABLE_WEST_ACTIVE else Sprite.CABLE_WEST
            }
        }
}

// ---------------------------------------------------------------------------------------------
// Pressure plates react to whatever stands on them after every move.
// ---------------------------------------------------------------------------------------------

abstract class PressurePlate(type: TerrainType, c: Coordinate) : Sand(type, c) {
    abstract fun interact(entity: Entity?, game: Game)
}

class BombField(type: TerrainType, c: Coordinate) : PressurePlate(type, c) {
    override fun interact(entity: Entity?, game: Game) {
        if (entity is Pushy) game.explodeBombs()
    }
}

class CoconutTunnelFinish(type: TerrainType, c: Coordinate) : PressurePlate(type, c) {
    private var lastEntity: Entity? = null

    override fun interact(entity: Entity?, game: Game) {
        if (lastEntity === entity) return
        lastEntity = entity
        if (entity is Coconut) game.checkCoconutTunnels()
    }

    override val isCoconutTunnel get() = true
    override val coconutCanEnterFromTunnel get() = true
}

/** Melts into water once something has stood on it and left again. */
class Ice(type: TerrainType, c: Coordinate) : PressurePlate(type, c) {
    private var wasVisited = false

    override val sprites get() = listOf(Sprite.WATER, Sprite.ICE)

    override fun interact(entity: Entity?, game: Game) {
        if (entity != null) {
            wasVisited = true
            return
        }
        if (wasVisited) game.board.setTerrain(coordinate, TerrainType.WATER)
    }
}

class InvisibleWaterPressurePlate(type: TerrainType, c: Coordinate) : PressurePlate(type, c) {
    override fun interact(entity: Entity?, game: Game) = game.updateInvisibleWater()
}

class SpikePressurePlate(type: TerrainType, c: Coordinate) : PressurePlate(type, c) {
    override val sprites get() = listOf(Sprite.SAND, Sprite.SPIKE_PRESSURE_PLATE)
    override fun interact(entity: Entity?, game: Game) = game.updateSpikes()
}

class LogicPressurePlate(type: TerrainType, c: Coordinate) : PressurePlate(type, c), LogicOutput {
    private var lastEntity: Entity? = null

    override fun isOutputActive(game: Game) = game.board.entityAt(this) != null
    override fun isOutput(direction: Direction) = true

    override fun interact(entity: Entity?, game: Game) {
        if (entity === lastEntity) return
        lastEntity = entity
        game.propagateAround(coordinate)
    }
}

/** Throws Pushy one field further in the direction he is facing. */
class Spring(type: TerrainType, c: Coordinate) : PressurePlate(type, c) {
    private var previousEntity: Entity? = null

    override fun interact(entity: Entity?, game: Game) {
        if (entity is Pushy && previousEntity !is Pushy) {
            game.teleport(coordinate, coordinate.next(entity.facing), jump = true)
            previousEntity = if (entity.isTeleported) null else entity
            return
        }
        previousEntity = entity
    }
}

class StringPressurePlate(type: TerrainType, c: Coordinate) : PressurePlate(type, c) {
    private var used = false

    override val sprites get() = listOf(if (used) Sprite.HOLE_FILLED else Sprite.STRING_PRESSURE_PLATE)

    override fun interact(entity: Entity?, game: Game) {
        if (entity is Pushy && !used) {
            used = true
            game.toggleString()
        }
    }
}

open class Teleporter(type: TerrainType, c: Coordinate) : PressurePlate(type, c) {

    open fun canTeleport(entity: Entity?) = entity is Pushy && !entity.isTeleported

    override fun interact(entity: Entity?, game: Game) {
        if (!canTeleport(entity)) return
        val other = game.board.terrainsOf<Teleporter>()
            .firstOrNull { it !== this && it::class == this::class } ?: return
        game.teleport(coordinate, other.coordinate)
    }
}

class ItemTeleporter(type: TerrainType, c: Coordinate) : Teleporter(type, c) {
    override fun canTeleport(entity: Entity?) = entity != null && !entity.isTeleported
}
