package markus.wieland.pushygame.engine.level

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.model.TileType

/**
 * Level file format v2 – see `level-format/README.md`.
 *
 * Each row of [terrain] / [entities] is a string with one character per cell; [legend] maps the
 * characters to tile ids. [EMPTY] in the entity layer means "no entity".
 */
@Serializable
data class LevelFile(
    val format: Int,
    val name: String,
    val width: Int,
    val height: Int,
    val legend: Map<String, String>,
    val terrain: List<String>,
    val entities: List<String>,
)

class LevelFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

object LevelFormat {
    const val VERSION = 2
    const val EMPTY = '.'

    /** Characters handed out (in order) to tiles without a free preferred symbol. */
    val SYMBOL_POOL: String = run {
        val preferred = TileType.all.mapNotNull { it.symbol }.toSet() + EMPTY
        val candidates = ('a'..'z') + ('A'..'Z') + ('0'..'9') + "!#$%&()*,/:;<>?@[]^_`{|}'".toList() +
            ('À'..'ÿ').filterNot { it == '×' || it == '÷' }
        candidates.filterNot { it in preferred }.joinToString("")
    }

    private val compactJson = Json { encodeDefaults = true }
    private val prettyJson = Json { encodeDefaults = true; prettyPrint = true; prettyPrintIndent = "  " }
    private val lenientJson = Json { ignoreUnknownKeys = true }

    fun encode(level: LevelData): LevelFile {
        val used = LinkedHashSet<TileType>()
        level.terrain.forEach { used += it }
        level.entities.forEach { if (it != null) used += it }

        val symbols = LinkedHashMap<TileType, Char>()
        val taken = mutableSetOf(EMPTY)
        val pool = SYMBOL_POOL.iterator()
        for (type in used) {
            val preferred = type.symbol
            val symbol = if (preferred != null && preferred !in taken) preferred else {
                generateSequence { if (pool.hasNext()) pool.next() else null }.firstOrNull { it !in taken }
                    ?: throw LevelFormatException("Too many different tiles in one level")
            }
            taken += symbol
            symbols[type] = symbol
        }

        return LevelFile(
            format = VERSION,
            name = level.name,
            width = level.width,
            height = level.height,
            legend = symbols.entries.associate { (type, symbol) -> symbol.toString() to type.id },
            terrain = level.terrain.rows().map { row -> row.map { symbols.getValue(it) }.joinToString("") },
            entities = level.entities.rows().map { row ->
                row.map { if (it == null) EMPTY else symbols.getValue(it) }.joinToString("")
            },
        )
    }

    fun decode(file: LevelFile): LevelData {
        if (file.format != VERSION) throw LevelFormatException("Unsupported level format ${file.format}")
        if (file.width <= 0 || file.height <= 0) throw LevelFormatException("Invalid size ${file.width}x${file.height}")
        if (file.terrain.size != file.height || file.entities.size != file.height) {
            throw LevelFormatException("Expected ${file.height} rows per layer")
        }
        val legend = file.legend.mapKeys { (key, _) ->
            if (key.length != 1) throw LevelFormatException("Legend key '$key' must be a single character")
            key[0]
        }.mapValues { (symbol, id) ->
            TileType.fromId(id) ?: throw LevelFormatException("Unknown tile '$id' for symbol '$symbol'")
        }

        fun cell(rows: List<String>, row: Int, col: Int): Char {
            val line = rows[row]
            if (line.length != file.width) throw LevelFormatException("Row $row must have ${file.width} cells")
            return line[col]
        }

        val terrain = Grid(file.height, file.width) { c ->
            val symbol = cell(file.terrain, c.row, c.col)
            legend[symbol] as? TerrainType
                ?: throw LevelFormatException("Symbol '$symbol' at $c is not a terrain tile")
        }
        val entities = Grid(file.height, file.width) { c ->
            val symbol = cell(file.entities, c.row, c.col)
            if (symbol == EMPTY) null
            else legend[symbol] as? EntityType
                ?: throw LevelFormatException("Symbol '$symbol' at $c is not an entity tile")
        }
        return LevelData(file.name, terrain, entities)
    }

    fun toJson(level: LevelData, pretty: Boolean = true): String =
        (if (pretty) prettyJson else compactJson).encodeToString(LevelFile.serializer(), encode(level))

    fun fromJson(json: String): LevelData {
        val file = try {
            lenientJson.decodeFromString(LevelFile.serializer(), json)
        } catch (e: SerializationException) {
            throw LevelFormatException("Not a valid level file", e)
        } catch (e: IllegalArgumentException) {
            throw LevelFormatException("Not a valid level file", e)
        }
        return decode(file)
    }
}
