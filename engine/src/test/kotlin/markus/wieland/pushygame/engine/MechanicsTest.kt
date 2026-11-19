package markus.wieland.pushygame.engine

import markus.wieland.pushygame.engine.entity.Coconut
import markus.wieland.pushygame.engine.entity.Count
import markus.wieland.pushygame.engine.entity.Shot
import markus.wieland.pushygame.engine.logic.Lamp
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction.EAST
import markus.wieland.pushygame.engine.model.Direction.NORTH
import markus.wieland.pushygame.engine.model.Direction.SOUTH
import markus.wieland.pushygame.engine.model.Direction.WEST
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.terrain.Cable
import markus.wieland.pushygame.engine.terrain.Hole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MechanicsTest {

    @Test
    fun pushyWalksAndIsBlocked() {
        val game = Game(
            level(
                """
                ----
                --~-
                """,
                """
                .T..
                P...
                """,
            ),
        )
        game.move(NORTH)
        assertEquals("PT..\n....", game.entityRows(), "walks north")
        game.move(EAST)
        assertEquals("PT..\n....", game.entityRows(), "tree blocks")
        game.move(SOUTH)
        game.move(EAST)
        game.move(EAST)
        assertEquals(".T..\n.P..", game.entityRows(), "water blocks")
        assertEquals(Sprite.PUSHY_EAST, game.state.value.entities[Coordinate(1, 1)].single())
    }

    @Test
    fun boxFillsWater() {
        val game = Game(level("---~-", "PB..."))
        game.move(EAST)
        assertEquals(".PB..", game.entityRows())
        game.move(EAST)
        assertEquals("..P..", game.entityRows(), "box sank")
        game.move(EAST)
        assertEquals("...P.", game.entityRows(), "Pushy walks over the sunken box")
        assertEquals(TerrainType.BOX_WATER, game.board.terrain(Coordinate(0, 3)).type)
    }

    @Test
    fun grassIsHigherThanSand() {
        val game = Game(level("-==-", "P..."))
        game.move(EAST)
        assertEquals("P...", game.entityRows(), "cannot climb")
        val down = Game(level("==-", ".P."))
        down.move(EAST)
        assertEquals("..P", down.entityRows(), "can step down")
    }

    @Test
    fun collectsItemsAndPaysThePirate() {
        val game = Game(
            level(
                "-----",
                "PCcH.",
                entities = mapOf('C' to EntityType.COIN, 'c' to EntityType.COIN, 'H' to EntityType.PIRATE),
            ),
        )
        game.move(EAST)
        game.move(EAST)
        assertEquals(listOf(InventoryItem(Sprite.COIN, 2)), game.state.value.inventory)
        game.move(EAST)
        assertEquals("..P..", game.entityRows(), "pirate got paid and left")
        assertEquals(listOf(InventoryItem(Sprite.PIRATE, 1)), game.state.value.inventory)
    }

    @Test
    fun finishOnlyWhenGoalsAreMet() {
        val unfinished = Game(level("--\n--", "FS\nP.", entities = mapOf('S' to EntityType.SEA_STAR)))
        unfinished.move(NORTH)
        assertFalse(unfinished.state.value.isFinished, "sea star still on land")

        val finished = Game(level("--\n--", "F.\nP."))
        finished.move(EAST)
        finished.move(WEST)
        assertFalse(finished.state.value.isFinished, "finish only from the south")
        finished.move(NORTH)
        assertTrue(finished.state.value.isFinished)
    }

    @Test
    fun teleportersSwapPushy() {
        val game = Game(level("-t---t", "P.....", terrain = mapOf('t' to TerrainType.TELEPORTER)))
        game.move(EAST)
        assertEquals(".....P", game.entityRows())
        game.move(WEST)
        game.move(EAST)
        assertEquals(".P....", game.entityRows(), "stepping back on teleports again")
    }

    @Test
    fun leverPowersLampThroughCable() {
        val game = Game(
            level(
                "--+-\n----",
                "EL.A\nP...",
                terrain = mapOf('+' to TerrainType.CABLE),
                entities = mapOf('E' to EntityType.ENERGY, 'L' to EntityType.LEVER, 'A' to EntityType.LAMP),
            ),
        )
        val lamp = game.board.entityAt(Coordinate(0, 3)) as Lamp
        game.move(NORTH)
        assertFalse(lamp.isActive)
        game.move(EAST)
        assertTrue(lamp.isActive, "lever switched on")
        assertTrue((game.board.terrain(Coordinate(0, 2)) as Cable).isActive)
        assertEquals(Sprite.LEVER_ACTIVE, game.state.value.entities[Coordinate(0, 1)].single())
        game.move(EAST)
        assertTrue(lamp.isActive, "flipping the lever back needs another energy")
    }

    @Test
    fun notGateLightsLampAfterOneTick() {
        val game = Game(
            level(
                "---\n---\n---",
                ".A.\n.N.\nP..",
                entities = mapOf('A' to EntityType.LAMP, 'N' to EntityType.LOGIC_GATE_NOT),
            ),
        )
        val lamp = game.board.entityAt(Coordinate(0, 1)) as Lamp
        assertFalse(lamp.isActive)
        game.advance(299)
        assertFalse(lamp.isActive)
        game.advance(1)
        assertTrue(lamp.isActive)
        assertEquals(Sprite.LAMP_ACTIVE, game.state.value.entities[Coordinate(0, 1)].single())
    }

    @Test
    fun buttonReleasesAfterItsDuration() {
        val game = Game(
            level(
                "---\n---",
                "EBA\nP..",
                entities = mapOf('E' to EntityType.ENERGY, 'B' to EntityType.BUTTON, 'A' to EntityType.LAMP),
            ),
        )
        val lamp = game.board.entityAt(Coordinate(0, 2)) as Lamp
        game.move(NORTH)
        game.move(EAST)
        assertTrue(lamp.isActive)
        game.advance(950)
        assertFalse(lamp.isActive)
    }

    @Test
    fun coconutRollsIntoHole() {
        val game = Game(level("--h--", "PC...", terrain = mapOf('h' to TerrainType.HOLE), entities = mapOf('C' to EntityType.COCONUT)))
        game.move(EAST)
        assertTrue((game.board.terrain(Coordinate(0, 2)) as Hole).isFilled)
        assertEquals(".P...", game.entityRows())
        game.move(EAST)
        assertEquals("..P..", game.entityRows(), "filled hole is walkable")
    }

    @Test
    fun countsHideAgainAfterWrongOrder() {
        val game = Game(
            level(
                "-----\n-----\n-----",
                ".....\n.2.1.\n.P...",
                entities = mapOf('1' to EntityType.COUNT_ONE, '2' to EntityType.COUNT_TWO),
            ),
        )
        val counts = game.board.entitiesOf<Count>()
        assertTrue(counts.none { it.isUncovered })
        game.move(NORTH) // pushes "2" first – wrong order
        assertTrue(counts.single { it.value == 2 }.isUncovered)
        game.advance(500)
        assertTrue(counts.none { it.isUncovered })
    }

    @Test
    fun shotOpensShellAndTreeDropsCoconut() {
        val game = Game(
            level(
                "---\n---\n---\n---\n---",
                "...\n.T.\n.O.\nPY.\n.S.",
                entities = mapOf('O' to EntityType.SHELL, 'Y' to EntityType.SLINGSHOT, 'S' to EntityType.SHOT),
            ),
        )
        game.move(SOUTH)
        game.move(EAST) // collect the shot
        assertEquals(1, game.inventory.amount(Shot::class))
        game.move(NORTH) // walk against the slingshot from the south
        assertEquals(0, game.inventory.amount(Shot::class))
        assertEquals(Sprite.SHELL_OPEN_WITH_PEARL, game.state.value.entities[Coordinate(2, 1)].single())
        assertIs<Coconut>(game.board.entityAt(Coordinate(0, 1)))
    }

    @Test
    fun renderIdsAndEvents() {
        val game = Game(level("---~-", "PB..T"))
        val boxId = game.state.value.entityIds[Coordinate(0, 1)]!!
        game.move(EAST)
        assertEquals(boxId, game.state.value.entityIds[Coordinate(0, 2)], "ids follow pushed entities")
        game.move(EAST)
        assertEquals(listOf<GameEvent>(GameEvent.Consumed(boxId, Coordinate(0, 3))), game.state.value.events)
        game.move(EAST)
        game.move(EAST)
        assertEquals(listOf<GameEvent>(GameEvent.Blocked(EAST)), game.state.value.events, "tree blocks")
        game.move(WEST)
        assertTrue(game.state.value.events.isEmpty(), "events are drained per state")
    }

    @Test
    fun teleportEmitsEvent() {
        val game = Game(level("-t---t", "P.....", terrain = mapOf('t' to TerrainType.TELEPORTER)))
        val pushyId = game.pushy.id
        game.move(EAST)
        assertEquals(listOf<GameEvent>(GameEvent.Teleported(pushyId, Coordinate(0, 1))), game.state.value.events)
    }

    @Test
    fun springEmitsJump() {
        val game = Game(level("-s--", "P...", terrain = mapOf('s' to TerrainType.SPRING)))
        game.move(EAST)
        assertEquals("..P.", game.entityRows(), "spring launches Pushy one further")
        assertEquals(listOf<GameEvent>(GameEvent.Jumped(game.pushy.id, Coordinate(0, 1))), game.state.value.events)
    }
}
