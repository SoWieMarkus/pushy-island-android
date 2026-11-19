package markus.wieland.pushygame.engine.model

/** Fixed-size, row-major 2D grid. */
class Grid<T>(val height: Int, val width: Int, init: (Coordinate) -> T) : Iterable<T> {

    private val cells: MutableList<T> = MutableList(height * width) { init(Coordinate(it / width, it % width)) }

    fun isInside(c: Coordinate) = c.row in 0 until height && c.col in 0 until width

    operator fun get(c: Coordinate): T {
        require(isInside(c)) { "$c is outside of the ${height}x$width grid" }
        return cells[c.row * width + c.col]
    }

    operator fun get(row: Int, col: Int): T = get(Coordinate(row, col))

    operator fun set(c: Coordinate, value: T) {
        require(isInside(c)) { "$c is outside of the ${height}x$width grid" }
        cells[c.row * width + c.col] = value
    }

    val coordinates: List<Coordinate>
        get() = List(height * width) { Coordinate(it / width, it % width) }

    override fun iterator(): Iterator<T> = cells.iterator()

    fun <R> map(transform: (Coordinate, T) -> R): Grid<R> = Grid(height, width) { transform(it, this[it]) }

    fun copy(): Grid<T> = map { _, value -> value }

    fun rows(): List<List<T>> = cells.chunked(width)

    override fun equals(other: Any?) =
        other is Grid<*> && other.height == height && other.width == width && other.cells == cells

    override fun hashCode() = cells.hashCode()
}
