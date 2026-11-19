package markus.wieland.pushygame.engine.logic

import markus.wieland.pushygame.engine.Game
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.Field
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.engine.terrain.Cable

/**
 * A logic part that consumes a signal.
 *
 * Direction convention (kept from the original game): `isInput(d)` asks whether a signal travelling
 * in direction `d` can enter this field, i.e. whether the port facing `d.opposite` is an input.
 */
interface LogicInput {
    fun isInput(direction: Direction): Boolean
    fun update(game: Game)

    companion object {
        /** Whether the neighbour of [at] in [direction] currently feeds a signal into [at]. */
        fun isInputActive(game: Game, direction: Direction, at: Coordinate): Boolean {
            val c = at.next(direction)
            if (!game.board.isInside(c)) return false
            val entity = game.board.entityAt(c)
            val terrain = game.board.terrain(c)
            if (entity is LogicOutput && entity.isOutput(direction)) return entity.isOutputActive(game)
            if (terrain is LogicOutput && terrain.isOutput(direction)) return terrain.isOutputActive(game)
            return terrain is Cable && terrain.isActive
        }

        fun overlay(direction: Direction): Sprite = when (direction.opposite) {
            Direction.NORTH -> Sprite.INPUT_NORTH
            Direction.WEST -> Sprite.INPUT_WEST
            Direction.EAST -> Sprite.INPUT_EAST
            Direction.SOUTH -> Sprite.INPUT_SOUTH
        }
    }
}

/** A logic part that produces a signal. See [LogicInput] for the direction convention. */
interface LogicOutput {
    fun isOutputActive(game: Game): Boolean
    fun isOutput(direction: Direction): Boolean

    companion object {
        fun overlay(direction: Direction): Sprite = when (direction.opposite) {
            Direction.NORTH -> Sprite.OUTPUT_NORTH
            Direction.WEST -> Sprite.OUTPUT_WEST
            Direction.EAST -> Sprite.OUTPUT_EAST
            Direction.SOUTH -> Sprite.OUTPUT_SOUTH
        }
    }
}

/** Base sprite plus small arrows showing the input/output ports of a logic part. */
fun ioOverlay(field: Field, base: Sprite): List<Sprite> = buildList {
    add(base)
    for (direction in Direction.entries) {
        if (field is LogicInput && field.isInput(direction)) add(LogicInput.overlay(direction))
        if (field is LogicOutput && field.isOutput(direction)) add(LogicOutput.overlay(direction))
    }
}

/**
 * A connected network of cables plus the logic parts attached to it. Built fresh for every signal
 * change: the network is active when any attached output is active.
 */
class Edge {
    private val cables = mutableListOf<Cable>()
    private val outputs = mutableListOf<LogicOutput>()
    private val inputs = mutableListOf<LogicInput>()

    fun add(cable: Cable) = cables.add(cable)
    fun addInput(input: LogicInput) = inputs.add(input)
    fun addOutput(output: LogicOutput) = outputs.add(output)

    operator fun contains(cable: Cable) = cables.any { it === cable }
    fun containsInput(input: LogicInput) = inputs.any { it === input }
    fun containsOutput(output: LogicOutput) = outputs.any { it === output }

    fun update(game: Game) {
        var active = false
        for (output in outputs) active = active || output.isOutputActive(game)
        cables.forEach { it.isActive = active }
        inputs.forEach { it.update(game) }
    }
}
