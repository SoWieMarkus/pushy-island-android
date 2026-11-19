package markus.wieland.pushygame.engine.logic

import markus.wieland.pushygame.engine.Game
import markus.wieland.pushygame.engine.entity.Energy
import markus.wieland.pushygame.engine.entity.Entity
import markus.wieland.pushygame.engine.entity.GoalEntity
import markus.wieland.pushygame.engine.entity.Interactable
import markus.wieland.pushygame.engine.entity.Movable
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Sprite

enum class PortType { INPUT, OUTPUT, NONE }

/**
 * A stationary gate. Ports are described by the side of the gate they sit on. When the output
 * changes, the new signal is sent out one [TICK_MS] later.
 */
abstract class LogicGate(type: EntityType, c: Coordinate) : Entity(type, c), LogicInput, LogicOutput {

    private var currentOutput = false

    /** Which kind of port sits on the given [side] of the gate. */
    protected abstract fun port(side: Direction): PortType

    val inputSides: List<Direction> get() = Direction.entries.filter { port(it) == PortType.INPUT }
    val outputSides: List<Direction> get() = Direction.entries.filter { port(it) == PortType.OUTPUT }

    override val sprites get() = ioOverlay(this, type!!.sprite)

    override fun isInput(direction: Direction) = port(direction.opposite) == PortType.INPUT
    override fun isOutput(direction: Direction) = port(direction.opposite) == PortType.OUTPUT

    protected fun isInputActive(game: Game, side: Direction) = LogicInput.isInputActive(game, side, coordinate)

    override fun update(game: Game) {
        val output = isOutputActive(game)
        if (currentOutput == output) return
        currentOutput = output
        game.scheduler.schedule(TICK_MS) {
            outputSides.forEach { game.propagate(coordinate.next(it), it) }
        }
    }

    companion object {
        const val TICK_MS = 300L
    }
}

class AndGate(type: EntityType, c: Coordinate) : LogicGate(type, c) {
    override fun port(side: Direction) = when (side) {
        Direction.NORTH -> PortType.OUTPUT
        Direction.EAST, Direction.WEST -> PortType.INPUT
        Direction.SOUTH -> PortType.NONE
    }

    override fun isOutputActive(game: Game): Boolean {
        var output = true
        for (side in inputSides) output = isInputActive(game, side) && output
        return output
    }
}

class OrGate(type: EntityType, c: Coordinate) : LogicGate(type, c) {
    override fun port(side: Direction) = when (side) {
        Direction.NORTH -> PortType.OUTPUT
        Direction.EAST, Direction.WEST -> PortType.INPUT
        Direction.SOUTH -> PortType.NONE
    }

    override fun isOutputActive(game: Game): Boolean {
        var output = false
        for (side in inputSides) output = isInputActive(game, side) || output
        return output
    }
}

class XorGate(type: EntityType, c: Coordinate) : LogicGate(type, c) {
    override fun port(side: Direction) = when (side) {
        Direction.NORTH -> PortType.OUTPUT
        Direction.EAST, Direction.WEST -> PortType.INPUT
        Direction.SOUTH -> PortType.NONE
    }

    override fun isOutputActive(game: Game): Boolean {
        var output = false
        for (side in inputSides) output = output xor isInputActive(game, side)
        return output
    }
}

class NotGate(type: EntityType, c: Coordinate) : LogicGate(type, c) {
    override fun port(side: Direction) = when (side) {
        Direction.NORTH -> PortType.OUTPUT
        Direction.SOUTH -> PortType.INPUT
        else -> PortType.NONE
    }

    override fun isOutputActive(game: Game): Boolean {
        var input = false
        for (side in inputSides) input = isInputActive(game, side) || input
        return !input
    }
}

/** Lights up while any neighbour feeds it a signal. Every lamp has to be lit to finish the level. */
open class Lamp(type: EntityType, c: Coordinate) : LogicGate(type, c), GoalEntity {
    var isActive = false
        protected set

    override fun port(side: Direction) = PortType.INPUT
    override fun isInput(direction: Direction) = true

    override val sprites get() = listOf(if (isActive) Sprite.LAMP_ACTIVE else Sprite.LAMP)

    override fun isOutputActive(game: Game): Boolean {
        var active = false
        for (side in inputSides) active = isInputActive(game, side) || active
        isActive = active
        return isActive
    }

    override fun isSatisfied(game: Game) = isActive
}

/** Counts down 3 → 0 on every rising signal and stays lit once it reached zero. */
class CountDown(type: EntityType, c: Coordinate) : Lamp(type, c) {
    private var count = 3

    override val sprites
        get() = listOf(
            when (count) {
                0 -> Sprite.LAMP_ACTIVE
                1 -> Sprite.COUNT_DOWN_ONE
                2 -> Sprite.COUNT_DOWN_TWO
                else -> Sprite.COUNT_DOWN_THREE
            },
        )

    override fun update(game: Game) {
        var signal = false
        for (side in inputSides) signal = isInputActive(game, side) || signal
        if (!signal || count == 0) return
        count--
        if (count == 0) isActive = true
    }
}

/** Costs one energy to flip; stays in its state. */
class Lever(type: EntityType, c: Coordinate) : Interactable(type, c), LogicOutput {
    private var active = false

    override val sprites get() = listOf(if (active) Sprite.LEVER_ACTIVE else Sprite.LEVER)

    override fun canBeReachedFrom(side: Direction) = true

    override fun onInteract(direction: Direction, game: Game) {
        if (!game.inventory.take(Energy::class, 1)) return
        active = !active
        game.propagateAround(coordinate)
    }

    override fun isOutputActive(game: Game) = active
    override fun isOutput(direction: Direction) = true
}

/** Costs one energy to press; sends a signal for a few ticks. */
class Button(type: EntityType, c: Coordinate) : Interactable(type, c), LogicOutput {
    private var active = false

    override val sprites get() = listOf(if (active) Sprite.BUTTON_PRESSED else Sprite.BUTTON)

    override fun canBeReachedFrom(side: Direction) = true

    override fun onInteract(direction: Direction, game: Game) {
        if (active) return
        if (!game.inventory.take(Energy::class, 1)) return
        active = true
        game.propagateAround(coordinate)
        game.scheduler.schedule(ACTIVE_DURATION_MS) {
            active = false
            game.propagateAround(coordinate)
        }
    }

    override fun isOutputActive(game: Game) = active
    override fun isOutput(direction: Direction) = true

    companion object {
        const val ACTIVE_DURATION_MS = 3 * LogicGate.TICK_MS + 50
    }
}

/** A pushable block that always emits a signal. */
class PowerBlock(type: EntityType, c: Coordinate) : Movable(type, c), LogicOutput {
    override fun isOutputActive(game: Game) = true
    override fun isOutput(direction: Direction) = true

    override fun executeMove(next: Coordinate, game: Game) {
        val before = coordinate
        super.executeMove(next, game)
        powerUpdate(game, before, next)
    }

    fun powerUpdate(game: Game, before: Coordinate, after: Coordinate?) {
        for (direction in Direction.entries) {
            game.propagate(before.next(direction), direction)
            if (after != null) game.propagate(after.next(direction), direction)
        }
        game.updateCables()
    }
}

/** A pushable diode: passes a signal through in the direction it was last pushed. */
class Repeater(type: EntityType, c: Coordinate) : Movable(type, c), LogicInput, LogicOutput {
    private var currentOutput = false

    private val facing: Direction
        get() = when (type) {
            EntityType.REPEATER_EAST -> Direction.EAST
            EntityType.REPEATER_NORTH -> Direction.NORTH
            EntityType.REPEATER_SOUTH -> Direction.SOUTH
            else -> Direction.WEST
        }

    override val sprites get() = ioOverlay(this, type!!.sprite)

    override fun isInput(direction: Direction) = facing == direction
    override fun isOutput(direction: Direction) = facing == direction.opposite

    override fun isOutputActive(game: Game) = Direction.entries.any { side ->
        isInput(side.opposite) && LogicInput.isInputActive(game, side, coordinate)
    }

    override fun executeMove(next: Coordinate, game: Game) {
        val direction = coordinate.directionTo(next)
        super.executeMove(next, game)
        type = when (direction) {
            Direction.NORTH -> EntityType.REPEATER_NORTH
            Direction.SOUTH -> EntityType.REPEATER_SOUTH
            Direction.EAST -> EntityType.REPEATER_EAST
            else -> EntityType.REPEATER_WEST
        }
        game.updateCables()
        update(game)
    }

    override fun update(game: Game) {
        val output = isOutputActive(game)
        if (output == currentOutput) return
        currentOutput = output
        for (direction in Direction.entries) {
            if (isOutput(direction.opposite)) game.propagate(coordinate.next(direction), direction)
        }
    }
}
