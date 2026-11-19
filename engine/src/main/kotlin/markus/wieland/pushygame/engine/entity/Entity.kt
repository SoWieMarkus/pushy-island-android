package markus.wieland.pushygame.engine.entity

import markus.wieland.pushygame.engine.Game
import markus.wieland.pushygame.engine.logic.AndGate
import markus.wieland.pushygame.engine.logic.Button
import markus.wieland.pushygame.engine.logic.CountDown
import markus.wieland.pushygame.engine.logic.Lamp
import markus.wieland.pushygame.engine.logic.Lever
import markus.wieland.pushygame.engine.logic.NotGate
import markus.wieland.pushygame.engine.logic.OrGate
import markus.wieland.pushygame.engine.logic.PowerBlock
import markus.wieland.pushygame.engine.logic.Repeater
import markus.wieland.pushygame.engine.logic.XorGate
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Field
import markus.wieland.pushygame.engine.model.Sprite
import kotlin.reflect.KClass

abstract class Entity(open var type: EntityType?, coordinate: Coordinate) : Field(coordinate) {

    override val tileType: EntityType? get() = type

    /** Set while the entity stands on the teleporter it just arrived on, so it doesn't bounce back. */
    var isTeleported = false

    open val isExplodable: Boolean get() = false

    companion object {
        fun create(type: EntityType, c: Coordinate): Entity = when (type) {
            EntityType.STONE -> Stone(type, c)
            EntityType.FINISH -> Finish(type, c)
            EntityType.PUSHY -> Pushy(type, c)
            EntityType.BOX -> Box(type, c)
            EntityType.TREE -> Tree(type, c)
            EntityType.SEA_STAR -> SeaStar(type, c)
            EntityType.SEED -> Seed(type, c)
            EntityType.BOTTLE -> Bottle(type, c)
            EntityType.STATUE_RED, EntityType.STATUE_GREEN, EntityType.STATUE_BLUE -> Statue(type, c)
            EntityType.BUOY -> Buoy(type, c)
            EntityType.BOMB -> Bomb(type, c)
            EntityType.CRAB_MOTHER -> CrabMother(type, c)
            EntityType.CRAB_BABY -> CrabBaby(type, c)
            EntityType.SHOT -> Shot(type, c)
            EntityType.COCONUT -> Coconut(type, c)
            EntityType.SLINGSHOT -> Slingshot(type, c)
            EntityType.LEAF_DOWN, EntityType.LEAF_UP, EntityType.LEAF_LEFT, EntityType.LEAF_RIGHT -> Leaf(type, c)
            EntityType.COIN -> Coin(type, c)
            EntityType.KEY -> Key(type, c)
            EntityType.CHEST -> Chest(type, c)
            EntityType.PIRATE -> Pirate(type, c)
            EntityType.PIRATE_HUT -> PirateHut(type, c)
            EntityType.PEARL -> Pearl(type, c)
            EntityType.TOWER -> Tower(type, c)
            EntityType.SHELL, EntityType.SHELL_OPEN_WITH_PEARL -> Shell(type, c)
            EntityType.FLOWER_RED, EntityType.FLOWER_YELLOW -> Flower(type, c)
            EntityType.COUNT_ONE, EntityType.COUNT_TWO, EntityType.COUNT_THREE,
            EntityType.COUNT_FOUR, EntityType.COUNT_FIVE -> Count(type, c)
            EntityType.SHELL_1, EntityType.SHELL_2, EntityType.SHELL_3 -> PushShell(type, c)
            EntityType.BARREL -> Barrel(type, c)
            EntityType.OCTOPUS -> Octopus(type, c)
            EntityType.LEAF_CHANGER_EAST, EntityType.LEAF_CHANGER_WEST,
            EntityType.LEAF_CHANGER_NORTH, EntityType.LEAF_CHANGER_SOUTH -> LeafChanger(type, c)
            EntityType.ENERGY -> Energy(type, c)
            EntityType.LOGIC_GATE_NOT -> NotGate(type, c)
            EntityType.POWER_BLOCK -> PowerBlock(type, c)
            EntityType.LAMP -> Lamp(type, c)
            EntityType.LEVER -> Lever(type, c)
            EntityType.LOGIC_GATE_OR -> OrGate(type, c)
            EntityType.LOGIC_GATE_XOR -> XorGate(type, c)
            EntityType.LOGIC_GATE_AND -> AndGate(type, c)
            EntityType.COUNT_DOWN -> CountDown(type, c)
            EntityType.BUTTON -> Button(type, c)
            EntityType.REPEATER_NORTH, EntityType.REPEATER_SOUTH,
            EntityType.REPEATER_WEST, EntityType.REPEATER_EAST -> Repeater(type, c)
        }
    }
}

/** Entities that must be in a certain state for the level to count as solved. */
interface GoalEntity {
    fun isSatisfied(game: Game): Boolean
}

// ---------------------------------------------------------------------------------------------
// Static obstacles
// ---------------------------------------------------------------------------------------------

class Tree(type: EntityType, c: Coordinate) : Entity(type, c) {
    override fun destroysFlyingStone() = true
}

class Stone(type: EntityType, c: Coordinate) : Entity(type, c) {
    override val isExplodable get() = true
    override fun destroysFlyingStone() = true
}

class Buoy(type: EntityType, c: Coordinate) : Entity(type, c)

class PirateHut(type: EntityType, c: Coordinate) : Entity(type, c)

/** A piece of the string Pushy lays out between two string pressure plates. */
class StringPiece(c: Coordinate, from: Direction?, to: Direction) : Entity(null, c) {
    private val pieces = listOfNotNull(half(from), half(to.opposite))

    override val sprites get() = pieces

    private fun half(direction: Direction?): Sprite? = when (direction) {
        null -> null
        Direction.EAST -> Sprite.STRING_HALF_WEST
        Direction.WEST -> Sprite.STRING_HALF_EAST
        Direction.NORTH -> Sprite.STRING_HALF_SOUTH
        Direction.SOUTH -> Sprite.STRING_HALF_NORTH
    }
}

// ---------------------------------------------------------------------------------------------
// Collectibles
// ---------------------------------------------------------------------------------------------

abstract class Collectible(type: EntityType?, c: Coordinate) : Entity(type, c) {
    fun collect(game: Game) {
        game.inventory.add(this)
        game.board.removeEntity(this)
    }
}

class Coin(type: EntityType = EntityType.COIN, c: Coordinate = Coordinate.NONE) : Collectible(type, c)
class CrabBaby(type: EntityType, c: Coordinate) : Collectible(type, c)
class Energy(type: EntityType, c: Coordinate) : Collectible(type, c)
class Key(type: EntityType, c: Coordinate) : Collectible(type, c)
class Pearl(type: EntityType = EntityType.PEARL, c: Coordinate = Coordinate.NONE) : Collectible(type, c)
class Seed(type: EntityType, c: Coordinate) : Collectible(type, c)
class Shot(type: EntityType, c: Coordinate) : Collectible(type, c)

/** Inventory-only proof that the crab mother got all her babies back. */
class CrabMotherReward : Collectible(null, Coordinate.NONE) {
    override val sprites get() = listOf(Sprite.CRAB_MOTHER_HAPPY)
}

/** Inventory-only proof that the pirate got paid. */
class PirateReward : Collectible(null, Coordinate.NONE) {
    override val sprites get() = listOf(Sprite.PIRATE)
}

// ---------------------------------------------------------------------------------------------
// Interactable: Pushy walks against them instead of pushing them.
// ---------------------------------------------------------------------------------------------

abstract class Interactable(type: EntityType, c: Coordinate) : Entity(type, c) {

    /** [direction] is the direction Pushy walks in. */
    fun interact(direction: Direction, game: Game) {
        if (canBeReachedFrom(direction.opposite)) onInteract(direction, game)
    }

    /** [side] is the side of this entity Pushy is standing on. */
    protected abstract fun canBeReachedFrom(side: Direction): Boolean
    protected abstract fun onInteract(direction: Direction, game: Game)
}

class Finish(type: EntityType, c: Coordinate) : Interactable(type, c) {
    override fun canBeReachedFrom(side: Direction) = side == Direction.SOUTH
    override fun onInteract(direction: Direction, game: Game) = game.tryFinish()
}

class Flower(type: EntityType, c: Coordinate) : Interactable(type, c) {
    override fun canBeReachedFrom(side: Direction) = true
    override fun onInteract(direction: Direction, game: Game) = game.growFlowers(this, direction)
}

class LeafChanger(type: EntityType, c: Coordinate) : Interactable(type, c) {
    var direction: Direction = when (type) {
        EntityType.LEAF_CHANGER_EAST -> Direction.EAST
        EntityType.LEAF_CHANGER_WEST -> Direction.WEST
        EntityType.LEAF_CHANGER_SOUTH -> Direction.SOUTH
        else -> Direction.NORTH
    }
        private set

    override var type: EntityType?
        get() = when (direction) {
            Direction.EAST -> EntityType.LEAF_CHANGER_EAST
            Direction.WEST -> EntityType.LEAF_CHANGER_WEST
            Direction.NORTH -> EntityType.LEAF_CHANGER_NORTH
            Direction.SOUTH -> EntityType.LEAF_CHANGER_SOUTH
        }
        set(_) {}

    override fun canBeReachedFrom(side: Direction) = true

    override fun onInteract(direction: Direction, game: Game) {
        this.direction = direction
        game.board.entitiesOf<Leaf>().forEach { it.direction = direction }
    }
}

class Slingshot(type: EntityType, c: Coordinate) : Interactable(type, c) {
    override fun canBeReachedFrom(side: Direction) = side == Direction.SOUTH
    override fun onInteract(direction: Direction, game: Game) {
        if (game.inventory.take(Shot::class, 1)) game.shoot(this)
    }
}

/** Takes items of [currency] from the inventory and hands out a reward once it has [needed] of them. */
abstract class RewardEntity(
    type: EntityType,
    c: Coordinate,
    private val currency: KClass<out Collectible>,
) : Interactable(type, c) {
    var needed = 0
    var received = 0
        private set
    var gotReward = false
        private set

    protected abstract fun createReward(): Collectible?

    override fun onInteract(direction: Direction, game: Game) {
        if (gotReward) return
        val available = game.inventory.amount(currency).coerceAtMost(needed)
        if (available > 0) {
            game.inventory.take(currency, available)
            received += available
        }
        if (received >= needed) {
            gotReward = true
            createReward()?.let { game.inventory.add(it) }
        }
    }
}

class Chest(type: EntityType, c: Coordinate) : RewardEntity(type, c, Key::class) {
    init {
        needed = 1
    }

    override val sprites get() = listOf(if (gotReward) Sprite.CHEST_OPEN else Sprite.CHEST)
    override fun canBeReachedFrom(side: Direction) = side == Direction.SOUTH
    override fun createReward() = Coin()
}

class CrabMother(type: EntityType, c: Coordinate) : RewardEntity(type, c, CrabBaby::class), GoalEntity {
    override val sprites get() = listOf(if (gotReward) Sprite.CRAB_MOTHER_HAPPY else Sprite.CRAB_MOTHER)
    override fun canBeReachedFrom(side: Direction) = side == Direction.SOUTH
    override fun createReward() = CrabMotherReward()
    override fun isSatisfied(game: Game) = game.inventory.amount(CrabMotherReward::class) > 0
}

class Pirate(type: EntityType, c: Coordinate) : RewardEntity(type, c, Coin::class), GoalEntity {
    override fun canBeReachedFrom(side: Direction) = side == Direction.WEST
    override fun createReward() = PirateReward()

    override fun onInteract(direction: Direction, game: Game) {
        super.onInteract(direction, game)
        if (gotReward) game.board.removeEntity(this)
    }

    override fun isSatisfied(game: Game) = game.inventory.amount(PirateReward::class) > 0
}

class Tower(type: EntityType, c: Coordinate) : RewardEntity(type, c, Pearl::class) {
    init {
        needed = 1
    }

    override val sprites get() = listOf(if (gotReward) Sprite.TOWER_WITH_PEARL else Sprite.TOWER)
    override fun canBeReachedFrom(side: Direction) = true
    override fun createReward(): Collectible? = null

    override fun onInteract(direction: Direction, game: Game) {
        super.onInteract(direction, game)
        if (gotReward) game.removeTowersIfAllLit()
    }
}
