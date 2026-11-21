package markus.wieland.pushygame.render

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import markus.wieland.pushygame.audio.Sound
import markus.wieland.pushygame.engine.GameEvent
import markus.wieland.pushygame.engine.GameState
import markus.wieland.pushygame.engine.model.Coordinate
import markus.wieland.pushygame.engine.model.Direction
import markus.wieland.pushygame.engine.model.Grid
import markus.wieland.pushygame.engine.model.Sprite
import markus.wieland.pushygame.engine.terrain.Boat
import markus.wieland.pushygame.engine.terrain.Terrain
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

private const val STEP_MS = 110f
private const val FADE_MS = 170f
private const val CROSSFADE_MS = 130f
private const val BUMP_MS = 170f
private const val BLAST_MS = 420f
private const val JUMP_MS = 300f
private const val SHOT_STEP_MS = 45f
private const val PUFF_MS = 220f
private const val ENTER_MS = 380f
private const val CELEBRATE_MS = 900f

/** The same sound isn't played twice within this many milliseconds of one transition. */
private const val SOUND_GAP_MS = 60f

/** How long the foam a boat leaves behind stays on the water. */
private const val WAKE_MS = 1400f

/** Confetti colours for the level-finished burst. */
private val CONFETTI = listOf(Color(0xFFFFD54A), Color(0xFFFF6B6B), Color(0xFF6BD6FF), Color(0xFF8BE07A), Color(0xFFFFFFFF))

/** At most this many states wait behind the one being animated; older ones are skipped. */
private const val MAX_BACKLOG = 2

/** How much faster animations play while further moves are waiting. */
private const val CATCH_UP_SPEED = 1.8f

private val PUSHY_SPRITES = setOf(Sprite.PUSHY, Sprite.PUSHY_EAST, Sprite.PUSHY_SOUTH, Sprite.PUSHY_WEST)
private val COUNT_DOWN = setOf(Sprite.COUNT_DOWN_THREE, Sprite.COUNT_DOWN_TWO, Sprite.COUNT_DOWN_ONE)

/**
 * Game board that animates between consecutive [GameState]s: entities slide between cells, Pushy
 * hops, items pop in and out, bombs flash and multi-frame sprites (water, lamps, …) loop.
 *
 * The engine applies moves instantly; this composable queues the resulting states and plays them
 * back one after another, speeding up when the player is faster than the animations.
 * [onSound] is called in step with the animations (a splash when the box hits the water, …).
 */
@Composable
fun AnimatedBoard(
    state: GameState,
    sprites: SpriteSheet,
    modifier: Modifier = Modifier,
    startInset: Dp = 0.dp,
    endInset: Dp = 0.dp,
    onSound: (Sound) -> Unit = {},
) {
    val sound by rememberUpdatedState(onSound)
    val animator = remember { BoardAnimator(state) { sound(it) } }
    val edges = remember { TerrainEdgesCache() }
    LaunchedEffect(state) { animator.offer(state) }
    LaunchedEffect(animator) {
        while (true) withFrameMillis { animator.tick(it, keepTicking = sprites.hasAnimations) }
    }

    // Fills its whole area: the board sits centred between [startInset] and [endInset] and the sea continues
    // around it on the same grid, so the island floats in an endless ocean.
    Canvas(modifier) {
        val now = animator.clock // read on every frame → redraws while something animates
        val cols = state.terrain.width
        val rows = state.terrain.height
        val start = startInset.toPx()
        val usable = size.width - start - endInset.toPx()
        val side = floor(min(usable / cols, size.height / rows)).coerceAtLeast(1f)
        val cell = Size(side, side)
        // Drawn over the terrain, below everything else: soft shorelines first, then boat wakes.
        val overTerrain = { scope: DrawScope, terrain: Grid<List<Sprite>> ->
            edges.of(terrain).draw(scope, sprites, now, cell)
            animator.wakes.forEach { scope.drawWake(it, now, cell) }
        }
        val origin = Offset((start + (usable - side * cols) / 2).roundToInt().toFloat(), ((size.height - side * rows) / 2).roundToInt().toFloat())
        drawSea(sprites, now, cell, origin, cols, rows)
        translate(origin.x, origin.y) {
            val transition = animator.transition
            if (transition == null) {
                drawStill(animator.shown, sprites, now, cell, overTerrain)
            } else {
                transition.draw(this, sprites, now, animator.elapsed(now), cell, overTerrain)
            }
        }
    }
}

/** Water tiles everywhere outside the board, aligned to the board's grid. */
private fun DrawScope.drawSea(sprites: SpriteSheet, now: Long, cell: Size, origin: Offset, cols: Int, rows: Int) {
    val firstCol = -ceil(origin.x / cell.width).toInt()
    val firstRow = -ceil(origin.y / cell.height).toInt()
    val lastCol = ceil((size.width - origin.x) / cell.width).toInt()
    val lastRow = ceil((size.height - origin.y) / cell.height).toInt()
    for (row in firstRow until lastRow) {
        for (col in firstCol until lastCol) {
            if (row in 0 until rows && col in 0 until cols) continue
            val c = Coordinate(row, col)
            drawSprite(
                sprites.frame(Sprite.WATER, now + terrainPhase(c)),
                origin.x + col * cell.width, origin.y + row * cell.height, cell.width, cell.height,
            )
        }
    }
}

private class BoardAnimator(initial: GameState, private val onSound: (Sound) -> Unit) {
    var shown by mutableStateOf(initial)
        private set
    var transition by mutableStateOf<Transition?>(null)
        private set
    var clock by mutableLongStateOf(0L)
        private set

    /** Boat wakes still visible on the water; they outlive the transition that made them. */
    val wakes = mutableListOf<Wake>()

    private val backlog = ArrayDeque<GameState>()

    /** Sounds of the running transition that are still to come, with the clock time they are due. */
    private val pendingSounds = mutableListOf<Pair<Long, Sound>>()
    private var startedAt = 0L
    private var speed = 1f

    fun offer(state: GameState) {
        val latest = backlog.lastOrNull() ?: transition?.to ?: shown
        if (state.gameId == latest.gameId && state.version <= latest.version) return
        if (state.gameId != latest.gameId) {
            // A new game (restart): jump straight there.
            backlog.clear()
            wakes.clear()
            pendingSounds.clear()
            transition = null
            shown = state
            return
        }
        backlog.addLast(state)
        while (backlog.size > MAX_BACKLOG) backlog.removeFirst()
    }

    fun elapsed(now: Long) = (now - startedAt) * speed

    fun tick(now: Long, keepTicking: Boolean) {
        val current = transition
        if (current != null && elapsed(now) >= current.duration) {
            shown = current.to
            transition = null
        }
        if (transition == null && backlog.isNotEmpty()) {
            val next = backlog.removeFirst()
            val planned = Transition(shown, next)
            if (planned.duration <= 0f) {
                shown = next
            } else {
                transition = planned
                startedAt = now
                speed = if (backlog.isEmpty()) 1f else CATCH_UP_SPEED
                for (boat in planned.boats) wakes += Wake(boat, now, speed, next)
                for ((at, sound) in planned.sounds) pendingSounds += now + (at / speed).toLong() to sound
            }
        }
        val due = pendingSounds.filter { it.first <= now }
        if (due.isNotEmpty()) {
            pendingSounds.removeAll(due)
            due.forEach { onSound(it.second) }
        }
        wakes.removeAll { it.isGone(now) }
        if (keepTicking || transition != null || wakes.isNotEmpty() || pendingSounds.isNotEmpty()) clock = now
    }
}

private fun DrawScope.drawStill(
    state: GameState,
    sprites: SpriteSheet,
    now: Long,
    cell: Size,
    overTerrain: (DrawScope, Grid<List<Sprite>>) -> Unit,
) {
    for (c in state.terrain.coordinates) {
        drawCell(sprites, TerrainEdges.displayed(state.terrain[c]), c.col.toFloat(), c.row.toFloat(), cell, now + terrainPhase(c))
    }
    overTerrain(this, state.terrain)
    for (c in state.entities.coordinates) {
        // Once the level is finished Pushy is inside the hut.
        val layers = if (state.isFinished) state.entities[c].filterNot { it in PUSHY_SPRITES } else state.entities[c]
        drawCell(sprites, layers, c.col.toFloat(), c.row.toFloat(), cell, now + phase(c))
    }
}

/** Entities of the same kind shouldn't all blink in lockstep. */
private fun phase(c: Coordinate) = (c.row * 7L + c.col * 13L) * 53L

/** Water tiles each get their own timing so the sea doesn't pulse as one grid. */
internal fun terrainPhase(c: Coordinate): Long {
    var h = (c.row * 73856093) xor (c.col * 19349663)
    h = h xor (h ushr 13)
    return ((h * 1274126177) ushr 1).toLong() % 5000L
}

private fun DrawScope.drawCell(
    sprites: SpriteSheet,
    layers: List<Sprite>,
    col: Float,
    row: Float,
    cell: Size,
    now: Long,
    alpha: Float = 1f,
    scale: Float = 1f,
) {
    if (layers.isEmpty() || alpha <= 0f || scale <= 0f) return
    val w = cell.width * scale
    val h = cell.height * scale
    val left = col * cell.width + (cell.width - w) / 2
    val top = row * cell.height + (cell.height - h) / 2
    for (sprite in layers) drawSprite(sprites.frame(sprite, now), left, top, w, h, alpha)
}

private fun progress(t: Float, start: Float, duration: Float) =
    if (duration <= 0f) 1f else ((t - start) / duration).coerceIn(0f, 1f)

private fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (x - 1f).pow(3) + c1 * (x - 1f).pow(2)
}

private fun distance(a: Coordinate, b: Coordinate) = abs(a.row - b.row) + abs(a.col - b.col)

private class Slide(val sprites: List<Sprite>, val start: Coordinate, val end: Coordinate, val delay: Float = 0f) {
    val cells = distance(start, end)
    val duration = cells * STEP_MS
    val endTime get() = delay + duration

    /** Position (col, row) at time [t]. */
    fun at(t: Float): Offset {
        val raw = progress(t, delay, duration)
        val p = if (cells == 1) FastOutSlowInEasing.transform(raw) else 1f - (1f - raw).pow(2)
        return Offset(start.col + (end.col - start.col) * p, start.row + (end.row - start.row) * p)
    }
}

/** Foam a sailing boat leaves on the water, timed in the same animation clock as its [boat] slide. */
private class Wake(val boat: Slide, val startedAt: Long, val speed: Float, private val state: GameState) {
    /** Foam only shows on open water, never on the beach next to it. */
    fun isWater(col: Int, row: Int): Boolean {
        val c = Coordinate(row, col)
        if (row !in 0 until state.terrain.height || col !in 0 until state.terrain.width) return true
        return state.terrain[c].any { it == Sprite.WATER || it in Boat.SPRITES }
    }

    fun elapsed(now: Long) = (now - startedAt) * speed
    fun isGone(now: Long) = elapsed(now) > boat.endTime + WAKE_MS
}

private val FOAM = Color(0xFFEAFBFF)

/**
 * Two lines of foam spreading out in a V behind the boat, plus a churned streak down the middle,
 * all fading out a while after the boat has passed.
 */
private fun DrawScope.drawWake(wake: Wake, now: Long, cell: Size) {
    val boat = wake.boat
    if (boat.cells == 0) return
    val t = wake.elapsed(now)
    val pixel = cell.width / TILE_PIXELS
    val dirX = (boat.end.col - boat.start.col).toFloat() / boat.cells
    val dirY = (boat.end.row - boat.start.row).toFloat() / boat.cells
    fun dot(x: Float, y: Float, color: Color, size: Float = pixel) {
        if (!wake.isWater(floor(x / cell.width).toInt(), floor(y / cell.height).toInt())) return
        drawRect(color, topLeft = Offset(floor(x / pixel) * pixel, floor(y / pixel) * pixel), size = Size(size, size))
    }
    val samples = boat.cells * 6
    for (k in 0..samples) {
        val passedAt = boat.endTime * k / samples
        if (passedAt > t) break
        val age = (t - passedAt) / WAKE_MS
        if (age >= 1f) continue
        val alpha = (1f - age).pow(1.5f)
        val pos = boat.at(passedAt)
        // Start a little behind the boat's centre, at its stern.
        val cx = (pos.x + 0.5f - dirX * 0.3f) * cell.width
        val cy = (pos.y + 0.5f - dirY * 0.3f) * cell.height
        val spread = cell.width * (0.18f + 0.32f * (1f - (1f - age).pow(2)))
        dot(cx - dirY * spread, cy + dirX * spread, FOAM.copy(alpha = 0.85f * alpha))
        dot(cx + dirY * spread, cy - dirX * spread, FOAM.copy(alpha = 0.85f * alpha))
        if (k % 2 == 0) dot(cx, cy, FOAM.copy(alpha = 0.35f * alpha), pixel * 2)
    }
}

/** Everything needed to animate from [from] to [to]. Times are in milliseconds from the start. */
private class Transition(val from: GameState, val to: GameState) {

    private class Pop(val sprites: List<Sprite>, val at: Coordinate, val delay: Float, val appearing: Boolean)

    // --- terrain ------------------------------------------------------------------------------
    /** Cells whose terrain field moved here (boats); the field that was here before is drawn below it. */
    private val terrainSlides = mutableListOf<Slide>()
    val boats: List<Slide> get() = terrainSlides
    private val terrainUnder = mutableMapOf<Coordinate, List<Sprite>>()
    private val crossfades = mutableMapOf<Coordinate, List<Sprite>>()

    // --- entities -----------------------------------------------------------------------------
    private val stills = mutableListOf<Pair<Coordinate, List<Sprite>>>()
    private val slides = mutableListOf<Slide>()
    private val pops = mutableListOf<Pop>()
    private val sinks = mutableListOf<Slide>()
    private val teleports = mutableListOf<Pair<Slide, Coordinate>>()
    private val jumps = mutableListOf<Pair<Slide, Coordinate>>()
    private var shot: GameEvent.Shot? = null
    private var shotEnd = 0f

    /** Entities that stay put but change look (an opening shell) – old look until something arrives. */
    private val changed = mutableMapOf<Coordinate, List<Sprite>>()
    private var pushy: Slide? = null
    private var pushyHops = false
    private var bump: Direction? = null
    private val blasts = mutableListOf<Coordinate>()

    /** The hut Pushy walks into on the winning move, or null if this move doesn't finish the level. */
    private var finish: Coordinate? = null

    /** When something arrives at a cell – plates, pick-ups and terrain changes react then. */
    private val arrival = mutableMapOf<Coordinate, Float>()

    val duration: Float

    /** Sounds to play during this transition: (time in ms from the start, sound). */
    val sounds: List<Pair<Float, Sound>>

    init {
        val consumed = mutableMapOf<Int, Coordinate>()
        val teleported = mutableMapOf<Int, Coordinate>()
        val jumped = mutableMapOf<Int, Coordinate>()
        for (event in to.events) when (event) {
            is GameEvent.Blocked -> bump = event.direction
            is GameEvent.Consumed -> consumed[event.entityId] = event.at
            is GameEvent.Teleported -> teleported[event.entityId] = event.from
            is GameEvent.Exploded -> blasts += event.at
            is GameEvent.Jumped -> jumped[event.entityId] = event.from
            is GameEvent.Shot -> shot = event
        }
        shot?.let { fired ->
            fired.path.forEachIndexed { i, c -> arrival[c] = maxOf(arrival[c] ?: 0f, i * SHOT_STEP_MS) }
            shotEnd = (fired.path.size - 1) * SHOT_STEP_MS
        }

        // Terrain fields that moved (a boat sailing).
        val oldTerrain = HashMap<Int, Coordinate>()
        for (c in from.terrainIds.coordinates) oldTerrain[from.terrainIds[c]] = c
        for (c in to.terrainIds.coordinates) {
            val old = oldTerrain[to.terrainIds[c]]
            if (old != null && old != c && to.terrain[c].any { it in Boat.SPRITES }) {
                // Only the hull sails; the water stays where it is.
                terrainSlides += Slide(to.terrain[c].filter { it in Boat.SPRITES }, old, c)
                terrainUnder[c] = from.terrain[c]
            }
        }

        // Entities: where was each one before, where is it now?
        val before = HashMap<Int, Coordinate>()
        for (c in from.entityIds.coordinates) from.entityIds[c]?.let { before[it] = c }
        val after = HashMap<Int, Coordinate>()
        for (c in to.entityIds.coordinates) to.entityIds[c]?.let { after[it] = c }

        for ((id, end) in after) {
            val sprites = to.entities[end]
            val start = before[id]
            val isPushy = sprites.any { it in PUSHY_SPRITES }
            when {
                start == null -> Unit // handled below, once arrival times are known
                id in jumped -> {
                    val spring = jumped.getValue(id)
                    val slide = Slide(sprites, start, spring)
                    jumps += slide to end
                    arrival[end] = slide.endTime + JUMP_MS
                }
                id in teleported -> {
                    val plate = teleported.getValue(id)
                    val slide = Slide(sprites, start, plate)
                    teleports += slide to end
                    arrival[end] = slide.endTime + FADE_MS
                }
                start == end -> {
                    if (isPushy) pushy = Slide(sprites, start, end) else stills += end to sprites
                    if (from.entities[start] != sprites) changed[end] = from.entities[start]
                }
                else -> {
                    val slide = Slide(sprites, start, end)
                    arrival[end] = maxOf(arrival[end] ?: 0f, slide.endTime)
                    if (isPushy) {
                        pushy = slide
                        pushyHops = slide.cells == 1 &&
                            from.elevation[start] == Terrain.ELEVATION_GRASS && to.elevation[end] == Terrain.ELEVATION_SAND
                    } else {
                        slides += slide
                    }
                }
            }
        }
        for ((id, start) in before) {
            if (id in after) continue
            val at = consumed[id]
            if (at != null) {
                val slide = Slide(from.entities[start], start, at)
                sinks += slide
                arrival[at] = maxOf(arrival[at] ?: 0f, slide.endTime)
            }
        }
        for ((id, start) in before) {
            if (id in after || id in consumed) continue
            // Collected or destroyed: shrink away as whatever takes the cell arrives.
            pops += Pop(from.entities[start], start, (arrival[start] ?: 0f) * 0.5f, appearing = false)
        }
        for ((id, end) in after) {
            if (id in before) continue
            // Things that pop up after a shot (coconuts) wait for the stone.
            pops += Pop(to.entities[end], end, arrival[end] ?: shotEnd, appearing = true)
        }

        if (to.isFinished && !from.isFinished) {
            finish = pushy?.end?.let { Coordinate(it.row - 1, it.col) }?.takeIf { Sprite.FINISH in to.entities[it] }
            finish?.let { hut -> stills.removeAll { it.first == hut } }
        }

        for (c in to.terrain.coordinates) {
            if (c in terrainUnder || terrainSlides.any { it.start == c }) continue
            if (from.terrain[c] != to.terrain[c]) crossfades[c] = from.terrain[c]
        }

        duration = listOfNotNull(
            terrainSlides.maxOfOrNull { it.endTime },
            slides.maxOfOrNull { it.endTime },
            sinks.maxOfOrNull { it.endTime + FADE_MS },
            teleports.maxOfOrNull { it.first.endTime + FADE_MS * 2 },
            jumps.maxOfOrNull { it.first.endTime + JUMP_MS },
            shot?.let { shotEnd + if (it.hit) PUFF_MS else 0f },
            pushy?.endTime?.takeIf { it > 0f },
            pops.maxOfOrNull { it.delay + FADE_MS },
            crossfades.keys.maxOfOrNull { (arrival[it] ?: 0f) + CROSSFADE_MS },
            if (bump != null && pushy?.cells == 0) BUMP_MS else null,
            if (blasts.isNotEmpty()) BLAST_MS else null,
            if (finish != null) ENTER_MS + CELEBRATE_MS else null,
        ).maxOrNull() ?: 0f
        sounds = planSounds()
    }

    private fun planSounds(): List<Pair<Float, Sound>> {
        val out = mutableListOf<Pair<Float, Sound>>()
        fun play(at: Float, sound: Sound) {
            if (out.none { it.second == sound && abs(it.first - at) < SOUND_GAP_MS }) out += at to sound
        }
        val walker = pushy
        when {
            finish != null -> play(ENTER_MS * 0.6f, Sound.WIN)
            walker != null && walker.cells > 0 && terrainSlides.isEmpty() -> play(0f, Sound.STEP)
            walker != null && bump != null -> {
                // Walking against something: did it react (lever, chest, flower, …) or just block?
                val b = bump!!
                val front = Coordinate(walker.end.row + b.dRow, walker.end.col + b.dCol)
                val reacted = front in changed || pops.any { !it.appearing && it.at == front } ||
                    from.inventory != to.inventory || shot != null
                if (!reacted) play(0f, Sound.BUMP) else if (shot == null) play(0f, Sound.CLICK)
            }
        }
        if (terrainSlides.isNotEmpty()) play(0f, Sound.SAIL)
        if (slides.isNotEmpty()) play(0f, Sound.PUSH)
        for (sink in sinks) {
            val wet = to.terrain[sink.end].any { it == Sprite.WATER || it == Sprite.BOX_WATER || it == Sprite.WATER_HOLE }
            play(sink.endTime, if (wet) Sound.SPLASH else Sound.DROP)
        }
        if (blasts.isNotEmpty()) {
            play(0f, Sound.EXPLOSION)
        } else {
            pops.filter { !it.appearing }.forEach { play(it.delay, Sound.PICKUP) }
        }
        for ((slide, _) in teleports) play(slide.endTime, Sound.TELEPORT)
        for ((slide, _) in jumps) play(slide.endTime, Sound.JUMP)
        shot?.let {
            play(0f, Sound.SHOT)
            if (it.hit) play(shotEnd, Sound.HIT)
        }

        // Terrain that changed (ice melting, spikes, doors, …) sounds when the change shows.
        val settled = listOfNotNull(walker?.endTime, slides.maxOfOrNull { it.endTime }).maxOrNull() ?: 0f
        for ((c, old) in crossfades) {
            val sound = terrainSound(old, to.terrain[c]) ?: continue
            if (sound == Sound.DROP && sinks.any { it.end == c }) continue // the coconut already plopped in
            play(arrival[c] ?: settled, sound)
        }
        // Lamps lighting up (also when a timed signal arrives without a move).
        for ((c, old) in changed) {
            val now = to.entities[c]
            val lit = Sprite.LAMP_ACTIVE in now && Sprite.LAMP_ACTIVE !in old
            val counted = now.any { it in COUNT_DOWN } && now != old
            if (lit || counted) play(arrival[c] ?: settled, Sound.LAMP_ON)
        }
        return out
    }

    private fun terrainSound(old: List<Sprite>, new: List<Sprite>): Sound? {
        fun appeared(s: Sprite) = s in new && s !in old
        fun vanished(s: Sprite) = s in old && s !in new
        return when {
            vanished(Sprite.ICE) -> Sound.ICE_MELT
            appeared(Sprite.SPIKES_DEACTIVATED) -> Sound.SPIKES_DOWN
            appeared(Sprite.SPIKES) -> Sound.SPIKES_UP
            vanished(Sprite.DOOR) -> Sound.DOOR_OPEN
            appeared(Sprite.DOOR) -> Sound.DOOR_CLOSE
            appeared(Sprite.SPRING) -> Sound.GROW
            appeared(Sprite.SEED) -> Sound.PLANT
            appeared(Sprite.HOLE_FILLED) && vanished(Sprite.HOLE) -> Sound.DROP
            appeared(Sprite.WATER_INVISIBLE) || vanished(Sprite.WATER_INVISIBLE) -> Sound.REVEAL
            appeared(Sprite.CHANGEABLE_FLOWER_RED) || appeared(Sprite.CHANGEABLE_FLOWER_GREEN) -> Sound.REVEAL
            else -> null
        }
    }

    fun draw(
        scope: DrawScope,
        sprites: SpriteSheet,
        now: Long,
        t: Float,
        cell: Size,
        overTerrain: (DrawScope, Grid<List<Sprite>>) -> Unit,
    ) = with(scope) {

        // A little screen shake while a bomb goes off.
        val shake = if (blasts.isEmpty()) Offset.Zero else {
            val p = progress(t, 0f, BLAST_MS * 0.6f)
            val amp = cell.width * 0.07f * (1f - p)
            Offset(sin(t * 0.09f) * amp, cos(t * 0.11f) * amp)
        }

        translate(shake.x, shake.y) {
            // Terrain: the new layout, with old tiles fading out where something changed.
            for (c in to.terrain.coordinates) {
                val layers = TerrainEdges.displayed(terrainUnder[c] ?: to.terrain[c])
                val tNow = now + terrainPhase(c)
                drawCell(sprites, layers, c.col.toFloat(), c.row.toFloat(), cell, tNow)
                crossfades[c]?.let { old ->
                    val p = progress(t, arrival[c] ?: 0f, CROSSFADE_MS)
                    drawCell(sprites, TerrainEdges.displayed(old), c.col.toFloat(), c.row.toFloat(), cell, tNow, alpha = 1f - p)
                }
            }
            overTerrain(this, to.terrain)
            for (slide in terrainSlides) {
                val pos = slide.at(t)
                drawCell(sprites, slide.sprites, pos.x, pos.y, cell, now)
            }

            // Entities.
            for ((c, layers) in stills) {
                val look = changed[c]?.takeIf { t < (arrival[c] ?: 0f) } ?: layers
                drawCell(sprites, look, c.col.toFloat(), c.row.toFloat(), cell, now + phase(c))
            }
            for (pop in pops) {
                val p = progress(t, pop.delay, FADE_MS)
                val c = pop.at
                if (pop.appearing) {
                    if (t >= pop.delay) drawCell(sprites, pop.sprites, c.col.toFloat(), c.row.toFloat(), cell, now + phase(c), scale = easeOutBack(p))
                } else {
                    drawCell(sprites, pop.sprites, c.col.toFloat(), c.row.toFloat(), cell, now + phase(c), alpha = 1f - p, scale = 1f - 0.6f * p)
                }
            }
            for (slide in slides) {
                val pos = slide.at(t)
                drawCell(sprites, slide.sprites, pos.x, pos.y, cell, now)
            }
            for (slide in sinks) {
                val pos = slide.at(t)
                val p = progress(t, slide.endTime, FADE_MS)
                drawCell(sprites, slide.sprites, pos.x, pos.y + 0.2f * p, cell, now, alpha = 1f - p, scale = 1f - 0.5f * p)
            }
            for ((slide, end) in teleports) {
                val pos = slide.at(t)
                val out = progress(t, slide.endTime, FADE_MS)
                if (out < 1f) drawCell(sprites, slide.sprites, pos.x, pos.y, cell, now, alpha = 1f - out, scale = 1f - 0.5f * out)
                val inP = progress(t, slide.endTime + FADE_MS, FADE_MS)
                if (out >= 1f) drawCell(sprites, slide.sprites, end.col.toFloat(), end.row.toFloat(), cell, now, alpha = inP, scale = easeOutBack(inP))
                if (out > 0f) drawSparkle(Offset((pos.x + 0.5f) * cell.width, (pos.y + 0.5f) * cell.height), cell, out)
                if (inP > 0f) drawSparkle(Offset((end.col + 0.5f) * cell.width, (end.row + 0.5f) * cell.height), cell, inP)
            }
            for ((slide, end) in jumps) drawJump(slide, end, sprites, cell, now, t)
            val hut = finish
            if (hut != null) drawFinish(hut, sprites, cell, now, t) else pushy?.let { drawPushy(it, sprites, cell, now, t) }
            shot?.let { drawShot(it, sprites, cell, now, t) }

            for (at in blasts) drawBlast(at, cell, progress(t, 0f, BLAST_MS))
        }
    }

    private fun DrawScope.drawPushy(slide: Slide, sprites: SpriteSheet, cell: Size, now: Long, t: Float) {
        var pos = slide.at(t)
        val p = progress(t, 0f, slide.duration)
        when {
            // Jumping down from grass to sand: a proper arc.
            pushyHops -> pos = pos.copy(y = pos.y - 0.32f * sin(PI.toFloat() * p))
            // Walking: a small bob each step.
            slide.cells == 1 -> pos = pos.copy(y = pos.y - 0.08f * sin(PI.toFloat() * p))
            // Walked into something: nudge towards it and spring back.
            slide.cells == 0 && bump != null -> {
                val b = bump!!
                val k = 0.16f * sin(PI.toFloat() * progress(t, 0f, BUMP_MS))
                pos = Offset(pos.x + b.dCol * k, pos.y + b.dRow * k)
            }
        }
        if (pushyHops) {
            // Shadow stays on the ground while Pushy is in the air.
            val ground = slide.at(t)
            drawOval(
                Color.Black.copy(alpha = 0.25f * sin(PI.toFloat() * p)),
                topLeft = Offset((ground.x + 0.2f) * cell.width, (ground.y + 0.78f) * cell.height),
                size = Size(cell.width * 0.6f, cell.height * 0.18f),
            )
        }
        drawCell(sprites, slide.sprites, pos.x, pos.y, cell, now)
    }

    /** Pushy walks into the hut, which hops for joy and throws confetti. */
    private fun DrawScope.drawFinish(hut: Coordinate, sprites: SpriteSheet, cell: Size, now: Long, t: Float) {
        val walker = pushy ?: return
        val enter = progress(t, 0f, ENTER_MS)
        val celebrate = progress(t, ENTER_MS, CELEBRATE_MS)

        // The hut: a squash as Pushy steps in, then two little hops.
        val hop = if (celebrate <= 0f) 0f else abs(sin(2 * PI.toFloat() * celebrate)) * 0.22f * (1f - celebrate)
        val squash = 0.08f * sin(PI.toFloat() * progress(t, ENTER_MS * 0.6f, ENTER_MS * 0.6f))
        val hutX = hut.col.toFloat()
        val hutY = hut.row.toFloat()
        if (hop > 0f) {
            drawOval(
                Color.Black.copy(alpha = 0.22f * (1f - hop * 2f)),
                topLeft = Offset((hutX + 0.18f) * cell.width, (hutY + 0.8f) * cell.height),
                size = Size(cell.width * 0.64f, cell.height * 0.16f),
            )
        }
        val w = cell.width * (1f + squash)
        val h = cell.height * (1f - squash)
        val left = hutX * cell.width + (cell.width - w) / 2
        val top = (hutY - hop) * cell.height + (cell.height - h)
        for (sprite in to.entities[hut]) drawSprite(sprites.frame(sprite, now + phase(hut)), left, top, w, h)

        // Pushy: a hop towards the door, shrinking and fading as it disappears inside.
        if (enter < 1f) {
            val e = FastOutSlowInEasing.transform(enter)
            val x = walker.end.col.toFloat()
            val y = walker.end.row - 0.55f * e - 0.12f * sin(PI.toFloat() * enter)
            val fade = progress(t, ENTER_MS * 0.45f, ENTER_MS * 0.55f)
            drawCell(sprites, walker.sprites, x, y, cell, now, alpha = 1f - fade, scale = 1f - 0.45f * e)
        }

        // Confetti bursting out of the hut and drifting down.
        if (celebrate > 0f && celebrate < 1f) {
            val center = Offset((hutX + 0.5f) * cell.width, (hutY + 0.35f) * cell.height)
            val dot = cell.width / 9f
            val ease = 1f - (1f - celebrate).pow(3)
            for (i in 0 until 18) {
                val angle = -PI.toFloat() / 2 + (i - 8.5f) * 0.17f + ((i * 29) % 7 - 3) * 0.04f
                val speed = cell.width * (0.9f + 0.6f * ((i * 41) % 10) / 10f)
                val gravity = cell.height * 1.4f * celebrate * celebrate
                val pos = Offset(center.x + cos(angle) * speed * ease, center.y + sin(angle) * speed * ease + gravity)
                drawRect(
                    CONFETTI[i % CONFETTI.size].copy(alpha = (1f - celebrate).coerceIn(0f, 1f).pow(0.5f)),
                    topLeft = Offset(pos.x - dot / 2, pos.y - dot / 2),
                    size = Size(dot, if ((i + (t / 80f).toInt()) % 2 == 0) dot else dot / 2),
                )
            }
            drawSparkle(center, cell, progress(t, ENTER_MS, CELEBRATE_MS * 0.5f))
        }
    }

    /** Walks onto the spring, then gets flung in an arc to [end]. */
    private fun DrawScope.drawJump(slide: Slide, end: Coordinate, sprites: SpriteSheet, cell: Size, now: Long, t: Float) {
        if (t < slide.endTime) {
            val pos = slide.at(t)
            drawCell(sprites, slide.sprites, pos.x, pos.y, cell, now)
            return
        }
        val p = progress(t, slide.endTime, JUMP_MS)
        val e = FastOutSlowInEasing.transform(p)
        val x = slide.end.col + (end.col - slide.end.col) * e
        val y = slide.end.row + (end.row - slide.end.row) * e
        val height = 0.75f * sin(PI.toFloat() * p)
        drawOval(
            Color.Black.copy(alpha = 0.28f * (1f - height)),
            topLeft = Offset((x + 0.2f + height * 0.1f) * cell.width, (y + 0.78f) * cell.height),
            size = Size(cell.width * (0.6f - height * 0.2f), cell.height * 0.18f),
        )
        // squash on take-off and landing, stretch in the air
        val squash = if (p !in 0.12f..0.9f) 0.1f else 0f
        drawCell(sprites, slide.sprites, x, y - height, cell, now, scale = 1f + 0.12f * sin(PI.toFloat() * p) - squash)
    }

    /** The slingshot stone flying along its path, with a puff where it hits. */
    private fun DrawScope.drawShot(event: GameEvent.Shot, sprites: SpriteSheet, cell: Size, now: Long, t: Float) {
        val path = event.path
        if (path.size < 2) return
        fun along(f: Float): Offset {
            val i = f.toInt().coerceIn(0, path.size - 2)
            val k = (f - i).coerceIn(0f, 1f)
            val a = path[i]
            val b = path[i + 1]
            return Offset(a.col + (b.col - a.col) * k, a.row + (b.row - a.row) * k)
        }
        if (t < shotEnd) {
            val f = t / SHOT_STEP_MS
            for (j in 3 downTo 1) { // short motion trail
                val pos = along((f - j * 0.25f).coerceAtLeast(0f))
                drawCell(sprites, listOf(Sprite.SHOT), pos.x, pos.y - 0.15f, cell, now, alpha = 0.4f - j * 0.1f, scale = 1f - j * 0.15f)
            }
            val pos = along(f)
            drawCell(sprites, listOf(Sprite.SHOT), pos.x, pos.y - 0.15f, cell, now, scale = 1.15f)
        } else if (event.hit) {
            val end = path.last()
            val p = progress(t, shotEnd, PUFF_MS)
            val center = Offset((end.col + 0.5f) * cell.width, (end.row + 0.5f) * cell.height)
            val dot = cell.width / 8f
            for (i in 0 until 6) {
                val angle = i * (PI.toFloat() / 3) + 0.4f
                val d = cell.width * (0.1f + 0.35f * p)
                drawRect(
                    Color(0xFFB8BAC4).copy(alpha = 1f - p),
                    topLeft = Offset(center.x + cos(angle) * d - dot / 2, center.y + sin(angle) * d - dot / 2),
                    size = Size(dot, dot),
                )
            }
        }
    }

    private fun DrawScope.drawBlast(at: Coordinate, cell: Size, p: Float) {
        if (p >= 1f) return
        val center = Offset((at.col + 0.5f) * cell.width, (at.row + 0.5f) * cell.height)
        val radius = cell.width * (0.3f + 1.3f * (1f - (1f - p).pow(3)))
        drawCircle(Color(0xFFFF8A1F).copy(alpha = 0.85f * (1f - p)), radius, center)
        drawCircle(Color(0xFFFFE9A8).copy(alpha = (1f - p * 1.6f).coerceAtLeast(0f)), radius * 0.6f, center)
        val debris = cell.width / 7f
        for (i in 0 until 10) {
            val angle = i * (2 * PI.toFloat() / 10) + 0.3f
            val d = radius * (0.7f + 0.4f * ((i * 37) % 10) / 10f)
            drawRect(
                Color(0xFF3B2A20).copy(alpha = 1f - p),
                topLeft = Offset(center.x + cos(angle) * d - debris / 2, center.y + sin(angle) * d - debris / 2),
                size = Size(debris, debris),
            )
        }
    }

    private fun DrawScope.drawSparkle(center: Offset, cell: Size, p: Float) {
        val a = sin(PI.toFloat() * p)
        if (a <= 0f) return
        val dot = cell.width / 8f
        for (i in 0 until 6) {
            val angle = i * (PI.toFloat() / 3) + p * 2f
            val d = cell.width * (0.15f + 0.4f * p)
            drawRect(
                Color(0xFFB8F4FF).copy(alpha = a),
                topLeft = Offset(center.x + cos(angle) * d - dot / 2, center.y + sin(angle) * d - dot / 2),
                size = Size(dot, dot),
            )
        }
    }
}
