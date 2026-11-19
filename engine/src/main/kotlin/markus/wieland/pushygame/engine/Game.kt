package markus.wieland.pushygame.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import markus.wieland.pushygame.engine.board.Board
import markus.wieland.pushygame.engine.entity.Bomb
import markus.wieland.pushygame.engine.entity.Box
import markus.wieland.pushygame.engine.entity.Coconut
import markus.wieland.pushygame.engine.entity.Coin
import markus.wieland.pushygame.engine.entity.Collectible
import markus.wieland.pushygame.engine.entity.Count
import markus.wieland.pushygame.engine.entity.CrabBaby
import markus.wieland.pushygame.engine.entity.CrabMother
import markus.wieland.pushygame.engine.entity.Entity
import markus.wieland.pushygame.engine.entity.Flower
import markus.wieland.pushygame.engine.entity.GoalEntity
import markus.wieland.pushygame.engine.entity.Key
import markus.wieland.pushygame.engine.entity.Leaf
import markus.wieland.pushygame.engine.entity.Pirate
import markus.wieland.pushygame.engine.entity.Pushy
import markus.wieland.pushygame.engine.entity.SeaStar
import markus.wieland.pushygame.engine.entity.Shell
import markus.wieland.pushygame.engine.entity.Slingshot
import markus.wieland.pushygame.engine.entity.Statue
import markus.wieland.pushygame.engine.entity.StringPiece
import markus.wieland.pushygame.engine.entity.Tower
import markus.wieland.pushygame.engine.entity.Tree
import markus.wieland.pushygame.engine.level.LevelData
import markus.wieland.pushygame.engine.logic.Edge
import markus.wieland.pushygame.engine.logic.LogicInput
import markus.wieland.pushygame.engine.logic.LogicOutput
import markus.wieland.pushygame.engine.logic.NotGate
import markus.wieland.pushygame.engine.logic.PowerBlock
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.EntityType
import markus.wieland.pushygame.engine.model.Field
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.engine.model.TerrainType
import markus.wieland.pushygame.engine.terrain.Cable
import markus.wieland.pushygame.engine.terrain.ChangeableFlower
import markus.wieland.pushygame.engine.terrain.CoconutTunnelFinish
import markus.wieland.pushygame.engine.terrain.FlowerFinish
import markus.wieland.pushygame.engine.terrain.Hole
import markus.wieland.pushygame.engine.terrain.InvisibleWater
import markus.wieland.pushygame.engine.terrain.InvisibleWaterPressurePlate
import markus.wieland.pushygame.engine.terrain.PressurePlate
import markus.wieland.pushygame.engine.terrain.SpikePressurePlate
import markus.wieland.pushygame.engine.terrain.Spikes
import markus.wieland.pushygame.engine.terrain.Teleporter
import markus.wieland.pushygame.engine.terrain.Terrain
import markus.wieland.pushygame.engine.terrain.Water

/** Immutable picture of a running game for the UI. */
data class GameState(
    val terrain: Grid<List<Sprite>>,
    val entities: Grid<List<Sprite>>,
    val inventory: List<InventoryItem>,
    val moves: Int,
    val isFinished: Boolean,
    /** Stable ids of the terrain fields, so the renderer can follow moving terrain (boats). */
    val terrainIds: Grid<Int>,
    /** Stable ids of the entities (null for empty cells), so the renderer can animate moves. */
    val entityIds: Grid<Int?>,
    /** Terrain height per cell (see [Terrain.ELEVATION_SAND] etc.). */
    val elevation: Grid<Int>,
    /** What happened since the previous state – see [GameEvent]. */
    val events: List<GameEvent> = emptyList(),
    /** Increases with every published state, so two identical-looking states stay distinct. */
    val version: Long = 0,
    /** Different for every [Game] instance – a change means the level was restarted. */
    val gameId: Int = 0,
)

/**
 * One play-through of a level. Not thread-safe: call [move] and [advance] from one thread.
 *
 * All timed behaviour runs on [scheduler]; the UI drives it by calling [advance] every frame.
 */
class Game(level: LevelData) {

    val board: Board = Board.from(level)
    val inventory = Inventory()
    val scheduler = Scheduler()
    val pushy: Pushy = board.entitiesOf<Pushy>().singleOrNull()
        ?: throw IllegalArgumentException("A level needs exactly one Pushy")

    private var moves = 0
    private var version = 0L
    private val pendingEvents = mutableListOf<GameEvent>()
    var isFinished = false
        private set

    // Per-round flags: some plates only trigger their effect once per move.
    private var spikesUpdatedThisRound = false
    private var invisibleWaterUpdatedThisRound = false

    // Per-game state (these were global statics in the original game).
    private var coconutTunnelsDone = false
    private var expectedCount = 1
    var isStringActive = false
        private set
    internal var stringDirection: Direction? = null

    private val _state: MutableStateFlow<GameState>
    val state: StateFlow<GameState>

    init {
        board.entitiesOf<CrabMother>().forEach { it.needed = board.entitiesOf<CrabBaby>().size }
        board.entitiesOf<Pirate>().forEach {
            it.needed = board.entitiesOf<Coin>().size + board.entitiesOf<Key>().size
        }
        initialize()
        _state = MutableStateFlow(snapshot())
        state = _state.asStateFlow()
    }

    private fun initialize() {
        board.entitiesOf<NotGate>().forEach { it.update(this) }
        for (box in board.entitiesOf<Box>()) {
            if (board.terrain(box) is Water) {
                board.removeEntity(box)
                board.setTerrain(box.coordinate, TerrainType.BOX_WATER)
            }
        }
        board.entitiesOf<SeaStar>().filter { board.terrain(it) is Water }.forEach { board.removeEntity(it) }
        board.terrainsOf<InvisibleWater>().forEach { it.showAsWater = true }
        board.entitiesOf<Count>().forEach { it.isUncovered = false }
        board.entitiesOf<PowerBlock>().forEach { it.powerUpdate(this, it.coordinate, null) }
        triggerPressurePlates()
        updateCables()
        resetRound()
    }

    // -----------------------------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------------------------

    fun move(direction: Direction) {
        if (isFinished) return
        pushy.facing = direction
        val before = pushy.coordinate
        pushy.move(pushy.coordinate.next(direction), this)
        if (pushy.coordinate == before) emit(GameEvent.Blocked(direction))
        triggerPressurePlates()
        for (entity in board.entities) entity.isTeleported = board.terrain(entity) is Teleporter
        resetRound()
        moves++
        publish()
    }

    /** Advances the game clock; call this regularly (e.g. every frame) while the game is shown. */
    fun advance(ms: Long) {
        if (scheduler.advance(ms)) publish()
    }

    val hasPendingTimers: Boolean get() = scheduler.hasPendingTasks

    // -----------------------------------------------------------------------------------------
    // Win condition
    // -----------------------------------------------------------------------------------------

    internal fun tryFinish() {
        if (isSolved()) isFinished = true
    }

    fun isSolved(): Boolean = changeableFlowersMatch() && flowerFinishesFilled() && goalsSatisfied()

    private fun goalsSatisfied() = board.entities.all { it !is GoalEntity || it.isSatisfied(this) }

    private fun flowerFinishesFilled() = board.terrainsOf<FlowerFinish>().all {
        val entity = board.entityAt(it)
        entity is Flower && entity.type == it.flowerType
    }

    private fun changeableFlowersMatch() = board.terrainsOf<ChangeableFlower>().map { it.type }.distinct().size <= 1

    // -----------------------------------------------------------------------------------------
    // Events triggered by entities and terrain
    // -----------------------------------------------------------------------------------------

    private fun triggerPressurePlates() {
        for (plate in board.terrainsOf<PressurePlate>()) plate.interact(board.entityAt(plate), this)
    }

    private fun resetRound() {
        spikesUpdatedThisRound = false
        invisibleWaterUpdatedThisRound = false
    }

    internal fun explodeBombs() = board.entitiesOf<Bomb>().forEach { it.explode(this) }

    /** Once every coconut tunnel end holds a coconut, all holes get filled. */
    internal fun checkCoconutTunnels() {
        if (coconutTunnelsDone) return
        val ends = board.terrainsOf<CoconutTunnelFinish>()
        if (ends.any { board.entityAt(it) !is Coconut }) return
        board.terrainsOf<Hole>().forEach { it.fill() }
        coconutTunnelsDone = true
    }

    /** The invisible water shows as water while any of its pressure plates is free. */
    internal fun updateInvisibleWater() {
        if (invisibleWaterUpdatedThisRound) return
        invisibleWaterUpdatedThisRound = true
        val show = board.terrainsOf<InvisibleWaterPressurePlate>().any { board.entityAt(it) == null }
        board.terrainsOf<InvisibleWater>().forEach { it.showAsWater = show }
    }

    /** Spikes are out while any spike pressure plate is free. */
    internal fun updateSpikes() {
        if (spikesUpdatedThisRound) return
        spikesUpdatedThisRound = true
        val active = board.terrainsOf<SpikePressurePlate>().any { board.entityAt(it) == null }
        board.terrainsOf<Spikes>().forEach { it.setActive(active, board.entityAt(it)) }
    }

    /** First string plate starts laying a string, the second one flips every flower under it. */
    internal fun toggleString() {
        if (isStringActive) {
            for (flower in board.terrainsOf<ChangeableFlower>()) {
                if (board.entityAt(flower) is StringPiece) flower.toggle()
            }
            board.entitiesOf<StringPiece>().forEach { board.removeEntity(it) }
            isStringActive = false
            return
        }
        stringDirection = null
        isStringActive = true
    }

    /** Moves the entity at [from] to [to]; [jump] is purely cosmetic (springs launch, teleporters beam). */
    internal fun teleport(from: Coordinate, to: Coordinate, jump: Boolean = false) {
        if (!board.isInside(to)) return
        val target = board.entityAt(to)
        val entity = board.entityAt(from) ?: return
        if (target == null || target is Collectible) {
            emit(if (jump) GameEvent.Jumped(entity.id, from) else GameEvent.Teleported(entity.id, from))
            entity.isTeleported = true
            board.swapEntities(from, to)
        }
    }

    /** Pushy steps onto a boat and sails until the next field is no longer free water. */
    internal fun sail(from: Coordinate, next: Coordinate) {
        val direction = from.directionTo(next) ?: return
        var target = next
        while (board.isInside(target) && board.entityAt(target) == null &&
            board.terrain(target).elevation == Terrain.ELEVATION_WATER
        ) {
            target = target.next(direction)
        }
        val destination = target.next(direction.opposite)
        board.swapEntities(from, destination)
        board.swapTerrain(from, destination)
    }

    /** All statues of the pushed colour move together; Pushy follows into the freed field. */
    internal fun pushStatues(from: Coordinate, next: Coordinate) {
        val direction = from.directionTo(next) ?: return
        val pushed = board.entityAt(from) as? Statue ?: return
        val follower = board.entityAt(pushy.coordinate.next(pushy.facing.opposite))
        board.removeEntity(pushy)
        for (statue in board.entitiesOf<Statue>()) {
            val target = statue.coordinate.next(direction)
            if (statue.type == pushed.type && statue.isMovePossible(target, this)) {
                board.swapEntities(statue.coordinate, target)
            }
        }
        board.setEntity(pushy.coordinate.next(direction), pushy)
        pushy.afterMove(follower, this)
    }

    /** Grows flowers of the same colour across grass in [direction]. */
    internal fun growFlowers(flower: Flower, direction: Direction) {
        val type = flower.type ?: return
        var next = flower.coordinate.next(direction)
        while (board.isInside(next)) {
            val terrain = board.terrain(next)
            if (terrain.elevation != Terrain.ELEVATION_GRASS || board.entityAt(next) != null) break
            if (terrain is FlowerFinish && terrain.oppositeFlowerType == type) break
            board.setEntity(next, Entity.create(type, next))
            next = next.next(direction)
        }
    }

    /** Fires a stone northwards; leaves redirect it, trees drop a coconut behind themselves. */
    internal fun shoot(slingshot: Slingshot) {
        var direction = Direction.NORTH
        var next = slingshot.coordinate.next(direction)
        var steps = 0
        val path = mutableListOf(slingshot.coordinate)
        var hit = false
        while (board.isInside(next) && steps < MAX_SHOT_STEPS) {
            if (path.size < MAX_SHOT_PATH) path += next
            val terrain = board.terrain(next)
            val entity = board.entityAt(next)
            if (entity is Leaf) direction = entity.direction
            next = next.next(direction)
            if (entity?.destroysFlyingStone() == true || terrain.destroysFlyingStone()) {
                hit = true
                if (entity is Tree && board.isInside(next) && board.terrain(next).elevation == Terrain.ELEVATION_SAND) {
                    board.setEntity(next, Entity.create(EntityType.COCONUT, next))
                }
                break
            }
            if (entity is Shell) entity.isOpen = true
            steps++
        }
        if (!hit && path.size < MAX_SHOT_PATH) path += next // flies off the board
        emit(GameEvent.Shot(path, hit))
    }

    /** Count blocks must be uncovered in order; a wrong one hides all of them again shortly after. */
    internal fun onCountUncovered(value: Int) {
        if (value != expectedCount) {
            expectedCount = 1
            scheduler.schedule(COUNT_RESET_DELAY_MS) {
                board.entitiesOf<Count>().forEach { it.isUncovered = false }
            }
            return
        }
        expectedCount++
    }

    internal fun emit(event: GameEvent) {
        pendingEvents += event
    }

    /** [entity] is about to vanish where it stands – lets the renderer finish its move first. */
    internal fun consumed(entity: Entity) = emit(GameEvent.Consumed(entity.id, entity.coordinate))

    internal fun removeTowersIfAllLit() {
        val towers = board.entitiesOf<Tower>()
        if (towers.all { it.gotReward }) towers.forEach { board.removeEntity(it) }
    }

    // -----------------------------------------------------------------------------------------
    // Logic signals
    // -----------------------------------------------------------------------------------------

    /** Sends a signal change travelling in [direction] into the field at [at]. */
    fun propagate(at: Coordinate, direction: Direction) {
        val edge = Edge()
        val entity = board.entityAt(at)
        val terrain = board.terrainAt(at)
        if (entity is LogicInput && entity.isInput(direction)) edge.addInput(entity)
        if (terrain is LogicInput && terrain.isInput(direction)) edge.addInput(terrain)
        if (terrain is Cable) {
            for (d in listOf(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
                collect(edge, d, terrain.coordinate)
            }
        }
        edge.update(this)
    }

    internal fun propagateAround(at: Coordinate) {
        for (direction in Direction.entries) propagate(at.next(direction), direction)
    }

    private fun collect(edge: Edge, direction: Direction, at: Coordinate) {
        val entity = board.entityAt(at)
        val terrain = board.terrainAt(at)
        fun isOutput(field: Field?) = field is LogicOutput && field.isOutput(direction) && !edge.containsOutput(field)
        fun isInput(field: Field?) = field is LogicInput && field.isInput(direction) && !edge.containsInput(field)
        when {
            isOutput(entity) -> edge.addOutput(entity as LogicOutput)
            isInput(entity) -> edge.addInput(entity as LogicInput)
            isOutput(terrain) -> edge.addOutput(terrain as LogicOutput)
            isInput(terrain) -> edge.addInput(terrain as LogicInput)
            terrain is Cable && terrain !in edge -> {
                edge.add(terrain)
                collect(edge, Direction.NORTH, at.next(Direction.NORTH))
                collect(edge, Direction.SOUTH, at.next(Direction.SOUTH))
                collect(edge, Direction.EAST, at.next(Direction.EAST))
                collect(edge, Direction.WEST, at.next(Direction.WEST))
            }
        }
    }

    internal fun updateCables() = board.terrainsOf<Cable>().forEach { it.updateConnections(board) }

    // -----------------------------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------------------------

    private fun snapshot(): GameState {
        val terrain = board.terrainSnapshot()
        val entities = board.entitySnapshot()
        val events = pendingEvents.toList()
        pendingEvents.clear()
        return GameState(
            terrain = terrain.map { _, t -> t.sprites },
            entities = entities.map { _, e -> e?.sprites.orEmpty() },
            inventory = inventory.snapshot(),
            moves = moves,
            isFinished = isFinished,
            terrainIds = terrain.map { _, t -> t.id },
            entityIds = entities.map { _, e -> e?.id },
            elevation = terrain.map { _, t -> t.elevation },
            events = events,
            version = version++,
            gameId = pushy.id,
        )
    }

    private fun publish() {
        _state.value = snapshot()
    }

    companion object {
        const val MAX_SHOT_STEPS = 1000
        private const val MAX_SHOT_PATH = 64
        const val COUNT_RESET_DELAY_MS = 500L
    }
}

/** Sprites of a level as it looks before the first move â€“ used for previews and the editor. */
fun LevelData.previewSprites(): Pair<Grid<List<Sprite>>, Grid<List<Sprite>>> {
    val board = Board.from(this)
    board.terrainsOf<Cable>().forEach { it.updateConnections(board) }
    return board.terrainSnapshot().map { _, t -> t.sprites } to
        board.entitySnapshot().map { _, e -> e?.sprites.orEmpty() }
}
