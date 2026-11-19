package markus.wieland.pushygame.engine

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import markus.wieland.pushygame.engine.level.LevelFormat
import markus.wieland.pushygame.engine.entity.Entity
import markus.wieland.pushygame.engine.logic.LogicInput
import markus.wieland.pushygame.engine.logic.LogicOutput
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.terrain.Terrain
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.engine.model.TileType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `level-format/tiles.json` is the tile table the web editor and the v1 converter use. It is generated
 * from the engine's enums; run the tests with `UPDATE_GENERATED=1` to regenerate it.
 */
class TileTableTest {

    private val file = File(repoRoot, "level-format/tiles.json")

    /** Logic ports per side of the tile ("input"/"output"), used by the editor to draw cables. */
    private fun ports(tile: TileType) = buildJsonObject {
        val field = when (tile) {
            is TerrainType -> Terrain.create(tile, Coordinate(0, 0))
            is EntityType -> Entity.create(tile, Coordinate(0, 0))
        }
        for (side in Direction.entries) {
            val kind = when {
                field is LogicInput && field.isInput(side.opposite) -> "input"
                field is LogicOutput && field.isOutput(side.opposite) -> "output"
                else -> null
            }
            if (kind != null) put(side.name.lowercase(), kind)
        }
    }

    /** Terrain family the editor's corner smoothing looks at ("sand", "grass", "water" or "other"). */
    private fun surface(tile: TileType): String = when (val field = (tile as? TerrainType)?.let { Terrain.create(it, Coordinate(0, 0)) }) {
        null -> "none"
        is markus.wieland.pushygame.engine.terrain.Grass -> "grass"
        is markus.wieland.pushygame.engine.terrain.Sand -> "sand"
        is markus.wieland.pushygame.engine.terrain.Water -> "water"
        else -> "other"
    }

    private fun generate(): String {
        val json = buildJsonObject {
            put("format", LevelFormat.VERSION)
            put("empty", LevelFormat.EMPTY.toString())
            put("symbolPool", LevelFormat.SYMBOL_POOL)
            put("v1NoEntity", EntityType.V1_NO_ENTITY)
            put("legacyNoEntity", EntityType.LEGACY_NO_ENTITY)
            putJsonArray("tiles") {
                for (tile in TileType.all) addJsonObject {
                    put("id", tile.id)
                    put("layer", tile.layer.name.lowercase())
                    put("sprite", tile.sprite.fileName)
                    put("maxInstances", tile.maxInstances?.let(::JsonPrimitive) ?: JsonPrimitive(null as Int?))
                    put("isLogic", tile.isLogic)
                    put("symbol", tile.symbol?.toString()?.let(::JsonPrimitive) ?: JsonPrimitive(null as String?))
                    put("v1Code", tile.v1Code)
                    put("legacyId", tile.legacyId)
                    put("ports", ports(tile))
                    put("surface", surface(tile))
                }
            }
        }
        return Json { prettyPrint = true; prettyPrintIndent = "  " }.encodeToString(json) + "\n"
    }

    @Test
    fun tileTableIsUpToDate() {
        val expected = generate()
        if (System.getenv("UPDATE_GENERATED") == "1") {
            file.parentFile.mkdirs()
            file.writeText(expected)
        }
        assertTrue(file.exists(), "Missing ${file.path}; run the engine tests with UPDATE_GENERATED=1")
        assertEquals(expected, file.readText().replace("\r\n", "\n"), "tiles.json is outdated; run with UPDATE_GENERATED=1")
    }

    @Test
    fun everySpriteFileExists() {
        val dir = File(repoRoot, "app/src/main/assets/sprites")
        val missing = Sprite.entries.filterNot { File(dir, "${it.fileName}.png").exists() }
        assertEquals(emptyList(), missing)
    }

    @Test
    fun tileIdsAndCodesAreUnique() {
        assertEquals(TileType.all.size, TileType.all.map { it.id }.toSet().size)
        for (layer in TileType.Layer.entries) {
            val tiles = TileType.all.filter { it.layer == layer }
            assertEquals(tiles.size, tiles.map { it.v1Code }.toSet().size, "v1 codes of $layer")
        }
    }
}
