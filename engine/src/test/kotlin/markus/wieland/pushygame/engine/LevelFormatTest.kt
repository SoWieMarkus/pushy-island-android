package markus.wieland.pushygame.engine

import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.level.LevelFormat
import markus.wieland.pushygame.engine.level.LevelFormatException
import markus.wieland.pushygame.engine.level.LevelProblem
import markus.wieland.pushygame.engine.level.ShareCode
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.model.TileType
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LevelFormatTest {

    private val fixtures = File(repoRoot, "level-format/fixtures")
    private val sampleJson get() = File(fixtures, "sample-level.json").readText()

    @Test
    fun jsonRoundTripIsStable() {
        val level = LevelFormat.fromJson(sampleJson)
        val written = LevelFormat.toJson(level)
        assertEquals(sampleJson.trimEnd().replace("\r\n", "\n"), written)
        assertEquals(level, LevelFormat.fromJson(written))
    }

    @Test
    fun shareCodeRoundTrip() {
        val level = LevelFormat.fromJson(sampleJson)
        val code = ShareCode.encode(level)
        assertTrue(code.startsWith(ShareCode.PREFIX))
        assertEquals(level, ShareCode.decode(code))
        assertEquals(level, ShareCode.decode("  " + code.chunked(40).joinToString("\n") + " "))
    }

    /** The web editor writes `sample-level.web.code`; this side writes `sample-level.kotlin.code`. */
    @Test
    fun shareCodesAreCompatibleWithTheWebEditor() {
        val level = LevelFormat.fromJson(sampleJson)
        val kotlinCode = File(fixtures, "sample-level.kotlin.code")
        if (System.getenv("UPDATE_GENERATED") == "1") kotlinCode.writeText(ShareCode.encode(level) + "\n")
        assertEquals(level, ShareCode.decode(kotlinCode.readText()))
        val webCode = File(fixtures, "sample-level.web.code")
        if (webCode.exists()) assertEquals(level, ShareCode.decode(webCode.readText()))
    }

    @Test
    fun everyTileSurvivesARoundTrip() {
        val terrains = TerrainType.entries
        val entities = EntityType.entries
        val base = LevelData.empty("All tiles", height = 8, width = 13)
        val terrain = base.terrain.copy()
        val entityGrid = base.entities.copy()
        terrains.forEachIndexed { i, t -> terrain[Coordinate(i / 13, i % 13)] = t }
        entities.forEachIndexed { i, e -> entityGrid[Coordinate(i / 13 + 3, i % 13)] = e }
        val level = base.copy(terrain = terrain, entities = entityGrid)
        assertEquals(level, LevelFormat.fromJson(LevelFormat.toJson(level)))
        assertEquals(level, ShareCode.decode(ShareCode.encode(level)))
        val symbols = LevelFormat.encode(level).legend.keys
        assertEquals(TileType.all.size, symbols.size)
    }

    @Test
    fun rejectsBrokenInput() {
        assertFailsWith<LevelFormatException> { LevelFormat.fromJson("{}") }
        assertFailsWith<LevelFormatException> { LevelFormat.fromJson(sampleJson.replace("\"format\": 2", "\"format\": 3")) }
        assertFailsWith<LevelFormatException> { LevelFormat.fromJson(sampleJson.replace("\"water\"", "\"lava\"")) }
        assertFailsWith<LevelFormatException> { ShareCode.decode("hello") }
        assertFailsWith<LevelFormatException> { ShareCode.decode("PUSHY2:AAAA") }
    }

    @Test
    fun reportsProblems() {
        val empty = LevelData.empty("Empty")
        assertEquals(listOf(LevelProblem.MissingPushy, LevelProblem.MissingFinish), empty.problems())
        val noName = LevelFormat.fromJson(sampleJson).copy(name = " ")
        assertEquals(listOf(LevelProblem.InvalidName), noName.problems())
    }
}
