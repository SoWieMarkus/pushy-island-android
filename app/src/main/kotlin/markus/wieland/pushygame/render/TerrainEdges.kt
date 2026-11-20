package markus.wieland.pushygame.render

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.engine.terrain.Boat

/**
 * Soft transitions between water, sand and grass.
 *
 * Terrain tiles are drawn one cell at a time, so two materials would otherwise meet in a hard
 * square line. This looks at each cell's neighbours and picks transparent overlays from
 * `sprites/edges/` (drawn by `tools/sprites/terrain.py`, which documents the naming):
 * surf on the water along a beach, a ledge shadow below grass hills and rounded outer corners. The hand-placed corner tiles are drawn as plain sand/grass plus the same
 * rounding, so they blend in with automatically rounded corners.
 *
 * Cells outside the board count as water, so the island's outer coast gets surf too.
 */
internal class TerrainEdges(private val terrain: Grid<List<Sprite>>) {

    private enum class Material(val key: String) { WATER("water"), SAND("sand"), GRASS("grass") }

    private class Piece(val at: Coordinate, val name: String)

    private val pieces: List<Piece> = buildList {
        for (row in -1..terrain.height) {
            for (col in -1..terrain.width) {
                val c = Coordinate(row, col)
                val inside = row in 0 until terrain.height && col in 0 until terrain.width
                val m = material(c)
                for (side in SIDES) {
                    val neighbour = Coordinate(row + side.dRow, col + side.dCol)
                    val n = material(neighbour)
                    if (n > m) {
                        // Stop short where the neighbour's corner is rounded: its bite carries the rim round the curve.
                        val cut = side.ends.filter { end -> biteAt(neighbour, side.cornerTowards(end)) != null }
                        add(Piece(c, "edge_${m.key}_${n.key}_${side.key}" + if (cut.isEmpty()) "" else "_cut_$cut"))
                    }
                    if (inside && n < m) add(Piece(c, "lip_${n.key}_${m.key}_${side.key}"))
                }
                for (corner in Corner.entries) {
                    val low = biteAt(c, corner) ?: continue
                    add(Piece(c, "bite_${low.key}_${m.key}_${corner.key}"))
                }
            }
        }
    }

    /** The material [corner] of the board cell [c] is rounded off into, or null if it stays square. */
    private fun biteAt(c: Coordinate, corner: Corner): Material? {
        if (c.row !in 0 until terrain.height || c.col !in 0 until terrain.width) return null
        val m = material(c)
        val placed = cornerOf(c)
        if (placed != null) return if (placed != corner) null else if (m == Material.SAND) Material.WATER else Material.SAND
        val (a, b) = corner.sides
        val sideA = material(Coordinate(c.row + a.dRow, c.col + a.dCol))
        val sideB = material(Coordinate(c.row + b.dRow, c.col + b.dCol))
        val diagonal = material(Coordinate(c.row + a.dRow + b.dRow, c.col + a.dCol + b.dCol))
        return sideA.takeIf { it == sideB && it < m && diagonal < m }
    }

    fun draw(scope: DrawScope, sprites: SpriteSheet, now: Long, cell: Size) {
        for (piece in pieces) {
            val frame = sprites.edge(piece.name, now + terrainPhase(piece.at)) ?: continue
            scope.drawSprite(frame, piece.at.col * cell.width, piece.at.row * cell.height, cell.width, cell.height)
        }
    }

    private fun material(c: Coordinate): Material {
        if (c.row !in 0 until terrain.height || c.col !in 0 until terrain.width) return Material.WATER
        val layers = terrain[c]
        return when {
            layers.any { it in GRASS } -> Material.GRASS
            layers.any { it in WATER } -> Material.WATER
            else -> Material.SAND
        }
    }

    private fun cornerOf(c: Coordinate): Corner? = terrain[c].firstNotNullOfOrNull { CORNER_TILES[it] }

    private class Side(val key: String, val dRow: Int, val dCol: Int) {
        /** The two ends of the strip along this side, in the order the sprite names use. */
        val ends: String get() = if (dRow != 0) "ew" else "ns"

        /** The corner of the neighbour on this side that touches the strip's [end]. */
        fun cornerTowards(end: Char): Corner {
            val vertical = if (dRow != 0) (if (dRow < 0) 's' else 'n') else end
            val horizontal = if (dRow != 0) end else (if (dCol < 0) 'e' else 'w')
            return Corner.entries.first { it.key == "$vertical$horizontal" }
        }
    }

    private enum class Corner(val key: String, val sides: Pair<Side, Side>) {
        NW("nw", NORTH to WEST), NE("ne", NORTH to EAST), SE("se", SOUTH to EAST), SW("sw", SOUTH to WEST),
    }

    companion object {
        private val NORTH = Side("n", -1, 0)
        private val EAST = Side("e", 0, 1)
        private val SOUTH = Side("s", 1, 0)
        private val WEST = Side("w", 0, -1)
        private val SIDES = listOf(NORTH, EAST, SOUTH, WEST)

        private val WATER = setOf(Sprite.WATER, Sprite.BOX_WATER) + Boat.SPRITES
        private val GRASS = setOf(
            Sprite.GRASS, Sprite.GRASS_TOP_LEFT, Sprite.GRASS_TOP_RIGHT, Sprite.GRASS_BOTTOM_LEFT, Sprite.GRASS_BOTTOM_RIGHT,
            Sprite.FLOWER_RED_PRESSURE_PLATE, Sprite.FLOWER_YELLOW_PRESSURE_PLATE,
        )
        private val CORNER_TILES = mapOf(
            Sprite.SAND_TOP_LEFT to Corner.NW, Sprite.SAND_TOP_RIGHT to Corner.NE,
            Sprite.SAND_BOTTOM_RIGHT to Corner.SE, Sprite.SAND_BOTTOM_LEFT to Corner.SW,
            Sprite.GRASS_TOP_LEFT to Corner.NW, Sprite.GRASS_TOP_RIGHT to Corner.NE,
            Sprite.GRASS_BOTTOM_RIGHT to Corner.SE, Sprite.GRASS_BOTTOM_LEFT to Corner.SW,
        )

        /** Corner tiles are drawn as their plain material; [TerrainEdges] adds the rounding. */
        fun displayed(layers: List<Sprite>): List<Sprite> =
            if (layers.none { it in CORNER_TILES }) layers else layers.map { sprite ->
                when (sprite) {
                    Sprite.SAND_TOP_LEFT, Sprite.SAND_TOP_RIGHT, Sprite.SAND_BOTTOM_LEFT, Sprite.SAND_BOTTOM_RIGHT -> Sprite.SAND
                    Sprite.GRASS_TOP_LEFT, Sprite.GRASS_TOP_RIGHT, Sprite.GRASS_BOTTOM_LEFT, Sprite.GRASS_BOTTOM_RIGHT -> Sprite.GRASS
                    else -> sprite
                }
            }
    }
}

/** Remembers the [TerrainEdges] of the last terrain grid, so they're only worked out when the terrain changes. */
internal class TerrainEdgesCache {
    private var terrain: Grid<List<Sprite>>? = null
    private var edges: TerrainEdges? = null

    fun of(terrain: Grid<List<Sprite>>): TerrainEdges {
        val cached = edges
        if (cached != null && this.terrain === terrain) return cached
        return TerrainEdges(terrain).also {
            this.terrain = terrain
            edges = it
        }
    }
}
