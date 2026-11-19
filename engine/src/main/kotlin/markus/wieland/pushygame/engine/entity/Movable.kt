package markus.wieland.pushygame.engine.entity

import markus.wieland.pushygame.engine.Game
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.terrain.BarrelFinish
import markus.wieland.pushygame.engine.terrain.Boat
import markus.wieland.pushygame.engine.terrain.Farm
import markus.wieland.pushygame.engine.terrain.Hole
import markus.wieland.pushygame.engine.terrain.StatueFinish
import markus.wieland.pushygame.engine.terrain.Terrain
import markus.wieland.pushygame.engine.terrain.Water
import markus.wieland.pushygame.engine.terrain.WaterHole

abstract class Movable(type: EntityType?, c: Coordinate) : Entity(type, c) {

    open val isPushableIntoWater: Boolean get() = false
    open val canBePushed: Boolean get() = true
    open val canCollect: Boolean get() = false
    open val canPush: Boolean get() = false
    open val canInteract: Boolean get() = false
    open val canUseBoat: Boolean get() = false

    /**
     * Whether this entity can move to the adjacent [next] cell. Has side effects, exactly like the
     * original game: collecting items, pushing other entities, interacting and sailing all happen here.
     */
    open fun isMovePossible(next: Coordinate, game: Game): Boolean {
        val board = game.board
        if (!board.isInside(next)) return false
        val direction = coordinate.directionTo(next) ?: return false

        val nextEntity = board.entityAt(next)
        val terrain = board.terrain(this)
        val nextTerrain = board.terrain(next)

        // Sailing: the boat event moves Pushy and the boat itself, so the regular move is cancelled.
        if (terrain is Boat && nextTerrain.elevation == Terrain.ELEVATION_WATER && canUseBoat) {
            game.sail(coordinate, next)
            return false
        }

        if (nextTerrain is Water) return isPushableIntoWater
        if (nextTerrain.elevation > terrain.elevation) return false
        if (nextEntity == null) return true

        // Items can be collected from any lower or equal field – including water. That's a feature.
        if (nextEntity is Collectible && canCollect) {
            nextEntity.collect(game)
            return true
        }

        if (terrain.hasSameElevationAs(nextTerrain)) {
            if (nextEntity is Movable && nextEntity.canBePushed && canPush) {
                return nextEntity.move(next.next(direction), game)
            }
            if (nextEntity is Interactable && canInteract) {
                nextEntity.interact(direction, game)
                return false
            }
        }
        return false
    }

    protected open fun executeMove(next: Coordinate, game: Game) {
        game.board.swapEntities(coordinate, next)
    }

    open fun move(next: Coordinate, game: Game): Boolean {
        if (!isMovePossible(next, game)) return false
        executeMove(next, game)
        return true
    }
}

class Pushy(type: EntityType, c: Coordinate) : Movable(type, c) {
    var facing = Direction.NORTH

    override val sprites
        get() = listOf(
            when (facing) {
                Direction.NORTH -> Sprite.PUSHY
                Direction.SOUTH -> Sprite.PUSHY_SOUTH
                Direction.WEST -> Sprite.PUSHY_WEST
                Direction.EAST -> Sprite.PUSHY_EAST
            },
        )

    override val canCollect get() = true
    override val canPush get() = true
    override val canInteract get() = true
    override val canUseBoat get() = true

    override fun executeMove(next: Coordinate, game: Game) {
        val follower = game.board.entityAt(coordinate.next(facing.opposite))
        super.executeMove(next, game)
        afterMove(follower, game)
    }

    /** Runs after Pushy arrived on a new cell. [follower] is whatever stood behind Pushy before. */
    fun afterMove(follower: Entity?, game: Game) {
        val previous = coordinate.next(facing.opposite)
        if (follower is Octopus) follower.move(previous, game)

        if (game.isStringActive && game.board.entityAt(previous) == null) {
            game.board.setEntity(previous, StringPiece(previous, game.stringDirection, facing))
            game.stringDirection = facing
        }

        val terrain = game.board.terrain(this)
        if (terrain is Farm && game.inventory.take(Seed::class, 1)) terrain.hasSeed = true
    }
}

class Box(type: EntityType, c: Coordinate) : Movable(type, c) {
    override val isPushableIntoWater get() = true
    override fun destroysFlyingStone() = true

    override fun executeMove(next: Coordinate, game: Game) {
        super.executeMove(next, game)
        if (game.board.terrain(this) is Water) {
            game.board.setTerrain(coordinate, TerrainType.BOX_WATER)
            game.board.removeEntity(this)
        }
    }
}

class SeaStar(type: EntityType, c: Coordinate) : Movable(type, c), GoalEntity {
    override val isPushableIntoWater get() = true

    override fun executeMove(next: Coordinate, game: Game) {
        super.executeMove(next, game)
        if (game.board.terrain(this).elevation == Terrain.ELEVATION_WATER) game.board.removeEntity(this)
    }

    /** Sea stars have to go back into the water. */
    override fun isSatisfied(game: Game) = false
}

class Bottle(type: EntityType, c: Coordinate) : Movable(type, c) {
    var isFilled = false
        private set

    override val sprites get() = listOf(if (isFilled) Sprite.BOTTLE_WITH_WATER else Sprite.BOTTLE)

    override fun executeMove(next: Coordinate, game: Game) {
        super.executeMove(next, game)
        val terrain = game.board.terrain(this)
        if (terrain is Farm && terrain.hasSeed && isFilled) {
            game.board.setTerrain(coordinate, TerrainType.SPRING)
            game.board.removeEntity(this)
        }
        if (terrain is WaterHole) isFilled = true
    }
}

class Statue(type: EntityType, c: Coordinate) : Movable(type, c), GoalEntity {

    /** Statues move as a group – see [Game.pushStatues]. Never report success so Pushy isn't moved twice. */
    override fun move(next: Coordinate, game: Game): Boolean {
        if (isMovePossible(next, game)) game.pushStatues(coordinate, next)
        return false
    }

    override fun isSatisfied(game: Game): Boolean {
        val terrain = game.board.terrain(this)
        return terrain is StatueFinish && terrain.accepts(type)
    }
}

class Bomb(type: EntityType, c: Coordinate) : Movable(type, c) {
    fun explode(game: Game) {
        for (direction in Direction.entries) {
            val entity = game.board.entityAt(coordinate.next(direction))
            if (entity != null && entity.isExplodable) game.board.removeEntity(entity)
        }
        game.board.removeEntity(this)
    }
}

/** Rolls until it hits something; fills the first open hole it rolls into. */
class Coconut(type: EntityType, c: Coordinate) : Movable(type, c) {

    override fun isMovePossible(next: Coordinate, game: Game): Boolean {
        val possible = super.isMovePossible(next, game)
        val nextTerrain = game.board.terrainAt(next) ?: return false
        if (game.board.terrain(this).isCoconutTunnel && !nextTerrain.coconutCanEnterFromTunnel) return false
        return possible || nextTerrain is Hole
    }

    override fun move(next: Coordinate, game: Game): Boolean {
        val direction = coordinate.directionTo(next) ?: return false
        var target = next
        var moved = false
        while (isMovePossible(target, game)) {
            moved = true
            executeMove(target, game)
            val terrain = game.board.terrain(this)
            if (terrain is Hole && !terrain.isFilled) {
                terrain.fill()
                game.board.removeEntity(this)
                break
            }
            target = target.next(direction)
        }
        return moved
    }
}

/** A number block that has to be uncovered in the right order (1, 2, 3, …). */
class Count(type: EntityType, c: Coordinate) : Movable(type, c), GoalEntity {
    val value = when (type) {
        EntityType.COUNT_TWO -> 2
        EntityType.COUNT_THREE -> 3
        EntityType.COUNT_FOUR -> 4
        EntityType.COUNT_FIVE -> 5
        else -> 1
    }
    var isUncovered = true

    override val sprites get() = listOf(if (isUncovered) type!!.sprite else Sprite.COUNT_HIDDEN)

    override fun isMovePossible(next: Coordinate, game: Game) = super.isMovePossible(next, game) && !isUncovered

    override fun executeMove(next: Coordinate, game: Game) {
        super.executeMove(next, game)
        isUncovered = true
        game.onCountUncovered(value)
    }

    override fun isSatisfied(game: Game) = isUncovered
}

class Leaf(type: EntityType, c: Coordinate) : Movable(type, c) {
    var direction: Direction
        get() = when (type) {
            EntityType.LEAF_DOWN -> Direction.SOUTH
            EntityType.LEAF_UP -> Direction.NORTH
            EntityType.LEAF_LEFT -> Direction.WEST
            else -> Direction.EAST
        }
        set(value) {
            type = when (value) {
                Direction.NORTH -> EntityType.LEAF_UP
                Direction.SOUTH -> EntityType.LEAF_DOWN
                Direction.EAST -> EntityType.LEAF_RIGHT
                Direction.WEST -> EntityType.LEAF_LEFT
            }
        }
}

/** Opens when hit by a shot; walking against an open shell takes its pearl. */
class Shell(type: EntityType, c: Coordinate) : Movable(type, c) {
    var isOpen = type == EntityType.SHELL_OPEN_WITH_PEARL
    private var hasPearl = true

    override var type: EntityType?
        get() = if (isOpen && hasPearl) EntityType.SHELL_OPEN_WITH_PEARL else EntityType.SHELL
        set(_) {}

    override val sprites
        get() = listOf(
            when {
                !isOpen -> Sprite.SHELL
                hasPearl -> Sprite.SHELL_OPEN_WITH_PEARL
                else -> Sprite.SHELL_OPEN
            },
        )

    override fun destroysFlyingStone() = isOpen

    override fun isMovePossible(next: Coordinate, game: Game) = super.isMovePossible(next, game) && !isOpen

    override fun move(next: Coordinate, game: Game): Boolean {
        if (super.move(next, game)) return true
        if (isOpen && hasPearl) {
            game.inventory.add(Pearl())
            hasPearl = false
        }
        return false
    }
}

/** Gets happier every time it is pushed (1 → 2 → 3 → 1 …). Has to end up happy. */
class PushShell(type: EntityType, c: Coordinate) : Movable(type, c), GoalEntity {
    private var mood = when (type) {
        EntityType.SHELL_3 -> HAPPY
        EntityType.SHELL_2 -> MEDIUM
        else -> UNHAPPY
    }

    override var type: EntityType?
        get() = when (mood) {
            HAPPY -> EntityType.SHELL_3
            MEDIUM -> EntityType.SHELL_2
            else -> EntityType.SHELL_1
        }
        set(_) {}

    override fun executeMove(next: Coordinate, game: Game) {
        mood = (mood + 1) % 3
        super.executeMove(next, game)
    }

    override fun isSatisfied(game: Game) = mood == HAPPY

    private companion object {
        const val UNHAPPY = 0
        const val MEDIUM = 1
        const val HAPPY = 2
    }
}

class Barrel(type: EntityType, c: Coordinate) : Movable(type, c), GoalEntity {
    override fun isSatisfied(game: Game) = game.board.terrain(this) is BarrelFinish
}

/** Follows Pushy around and must be brought to a water hole. */
class Octopus(type: EntityType, c: Coordinate) : Movable(type, c), GoalEntity {
    override val canBePushed get() = false

    override fun executeMove(next: Coordinate, game: Game) {
        super.executeMove(next, game)
        if (game.board.terrain(this) is WaterHole) game.board.removeEntity(this)
    }

    override fun isSatisfied(game: Game) = false
}
