package markus.wieland.pushygame.engine.model

enum class Direction(val dRow: Int, val dCol: Int) {
    NORTH(-1, 0),
    SOUTH(1, 0),
    EAST(0, 1),
    WEST(0, -1);

    val opposite: Direction
        get() = when (this) {
            NORTH -> SOUTH
            SOUTH -> NORTH
            EAST -> WEST
            WEST -> EAST
        }
}

/** A cell on the board. [row] grows southwards, [col] grows eastwards. */
data class Coordinate(val row: Int, val col: Int) {

    fun next(direction: Direction) = Coordinate(row + direction.dRow, col + direction.dCol)

    /** Direction of a straight line from this coordinate to [to], or null if it is not on one axis. */
    fun directionTo(to: Coordinate): Direction? {
        val dRow = to.row - row
        val dCol = to.col - col
        return when {
            dRow < 0 && dCol == 0 -> Direction.NORTH
            dRow > 0 && dCol == 0 -> Direction.SOUTH
            dRow == 0 && dCol < 0 -> Direction.WEST
            dRow == 0 && dCol > 0 -> Direction.EAST
            else -> null
        }
    }

    companion object {
        /** Placeholder for items that only live in the inventory. */
        val NONE = Coordinate(-1, -1)
    }
}
