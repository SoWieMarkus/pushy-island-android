package markus.wieland.pushygame.engine

import markus.wieland.pushygame.engine.level.LevelFormat
import markus.wieland.pushygame.engine.model.Direction
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CampaignTest {

    private val files = File(repoRoot, "app/src/main/assets/levels").listFiles { f -> f.extension == "json" }!!.sorted()

    @Test
    fun campaignIsComplete() {
        assertEquals(190, files.size)
        files.forEachIndexed { index, file ->
            assertTrue(file.name.startsWith("%03d-".format(index + 1)), "Unexpected file ${file.name}")
        }
    }

    @Test
    fun everyLevelLoadsAndIsPlayable() {
        for (file in files) {
            val level = LevelFormat.fromJson(file.readText())
            assertEquals(emptyList(), level.problems(), file.name)
            assertEquals(level, LevelFormat.fromJson(LevelFormat.toJson(level)), file.name)

            // Smoke test: wander around and let all timers run – nothing may crash.
            val game = Game(level)
            assertFalse(game.state.value.isFinished, file.name)
            repeat(3) {
                for (direction in Direction.entries) {
                    game.move(direction)
                    game.advance(1_000)
                }
            }
        }
    }
}
