package markus.wieland.pushygame.engine

import markus.wieland.pushygame.engine.editor.EditMode
import markus.wieland.pushygame.engine.editor.LevelEditor
import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.TerrainType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LevelEditorTest {

    private fun editor() = LevelEditor(LevelData.empty("Test", height = 4, width = 5))

    @Test
    fun fillUndoRedo() {
        val editor = editor()
        editor.apply(Coordinate(0, 0), TerrainType.SAND, EditMode.FILL)
        assertTrue(editor.level.terrain.all { it == TerrainType.SAND })
        editor.apply(Coordinate(1, 1), TerrainType.GRASS, EditMode.PENCIL)
        assertEquals(TerrainType.GRASS, editor.level.terrain[Coordinate(1, 1)])
        editor.undo()
        assertEquals(TerrainType.SAND, editor.level.terrain[Coordinate(1, 1)])
        editor.undo()
        assertTrue(editor.level.terrain.all { it == TerrainType.WATER })
        assertFalse(editor.canUndo)
        editor.redo()
        editor.redo()
        assertEquals(TerrainType.GRASS, editor.level.terrain[Coordinate(1, 1)])
    }

    @Test
    fun aStrokeIsOneUndoStep() {
        val editor = editor()
        editor.apply(Coordinate(0, 0), TerrainType.SAND, EditMode.PENCIL)
        editor.apply(Coordinate(1, 0), TerrainType.SAND, EditMode.PENCIL)
        editor.apply(Coordinate(1, 1), TerrainType.SAND, EditMode.PENCIL, continueStroke = true)
        editor.apply(Coordinate(1, 2), TerrainType.SAND, EditMode.PENCIL, continueStroke = true)
        editor.undo()
        assertEquals(1, editor.level.terrain.count { it == TerrainType.SAND })
    }

    @Test
    fun singletonsMoveInsteadOfDuplicating() {
        val editor = editor()
        editor.place(Coordinate(0, 0), EntityType.PUSHY)
        editor.place(Coordinate(2, 2), EntityType.PUSHY)
        assertNull(editor.level.entities[Coordinate(0, 0)])
        assertEquals(EntityType.PUSHY, editor.level.entities[Coordinate(2, 2)])

        editor.place(Coordinate(0, 0), TerrainType.TELEPORTER)
        editor.place(Coordinate(0, 1), TerrainType.TELEPORTER)
        editor.place(Coordinate(0, 2), TerrainType.TELEPORTER)
        assertEquals(2, editor.level.terrain.count { it == TerrainType.TELEPORTER })
    }

    @Test
    fun pirateComesWithHut() {
        val editor = editor()
        editor.place(Coordinate(3, 1), EntityType.PIRATE)
        assertEquals(EntityType.PIRATE, editor.level.entities[Coordinate(2, 1)])
        assertEquals(EntityType.PIRATE_HUT, editor.level.entities[Coordinate(3, 1)])
    }

    @Test
    fun eraseRemovesEntityFirst() {
        val editor = editor()
        editor.place(Coordinate(1, 1), TerrainType.SAND)
        editor.place(Coordinate(1, 1), EntityType.TREE)
        editor.erase(Coordinate(1, 1))
        assertNull(editor.level.entities[Coordinate(1, 1)])
        assertEquals(TerrainType.SAND, editor.level.terrain[Coordinate(1, 1)])
        editor.erase(Coordinate(1, 1))
        assertEquals(TerrainType.WATER, editor.level.terrain[Coordinate(1, 1)])
    }

    @Test
    fun smoothRoundsIslandCorners() {
        val editor = editor()
        for (row in 1..2) for (col in 1..3) editor.place(Coordinate(row, col), TerrainType.SAND)
        editor.smooth()
        assertEquals(TerrainType.SAND_TOP_LEFT, editor.level.terrain[Coordinate(1, 1)])
        assertEquals(TerrainType.SAND_TOP_RIGHT, editor.level.terrain[Coordinate(1, 3)])
        assertEquals(TerrainType.SAND_BOTTOM_LEFT, editor.level.terrain[Coordinate(2, 1)])
        assertEquals(TerrainType.SAND_BOTTOM_RIGHT, editor.level.terrain[Coordinate(2, 3)])
        assertEquals(TerrainType.SAND, editor.level.terrain[Coordinate(1, 2)])
    }
}
