package markus.wieland.pushygame.engine

import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction

/**
 * Things that happened during one state change which the renderer cannot infer from the
 * before/after snapshots alone. Purely cosmetic – the game logic never reads them.
 */
sealed interface GameEvent {
    /** Pushy tried to move in [direction] but stayed where it was. */
    data class Blocked(val direction: Direction) : GameEvent

    /** Entity [entityId] stepped onto the teleporter at [from] and was moved elsewhere. */
    data class Teleported(val entityId: Int, val from: Coordinate) : GameEvent

    /** Entity [entityId] stepped onto the spring at [from] and was launched to its new cell. */
    data class Jumped(val entityId: Int, val from: Coordinate) : GameEvent

    /**
     * A slingshot fired: the stone flew through [path] (starting at the slingshot). If it didn't
     * [hit] anything, the last cell is just outside the board.
     */
    data class Shot(val path: List<Coordinate>, val hit: Boolean) : GameEvent

    /** Entity [entityId] moved to [at] and vanished there (sank, filled a hole, …). */
    data class Consumed(val entityId: Int, val at: Coordinate) : GameEvent

    /** A bomb went off at [at]. */
    data class Exploded(val at: Coordinate) : GameEvent
}
