package markus.wieland.pushygame.engine

import java.util.PriorityQueue

/**
 * Deterministic game clock. Timed effects (logic gate delays, buttons, …) are queued here and run
 * when the UI – or a test – advances time.
 */
class Scheduler {
    var now: Long = 0
        private set

    private var sequence = 0L
    private val queue = PriorityQueue<Task>(compareBy<Task> { it.time }.thenBy { it.sequence })

    private class Task(val time: Long, val sequence: Long, val action: () -> Unit)

    val hasPendingTasks: Boolean get() = queue.isNotEmpty()

    fun schedule(delayMs: Long, action: () -> Unit) {
        queue += Task(now + delayMs, sequence++, action)
    }

    /** Advances the clock by [ms] and runs everything that became due. Returns whether anything ran. */
    fun advance(ms: Long): Boolean {
        val target = now + ms
        var ran = false
        while (true) {
            val next = queue.peek() ?: break
            if (next.time > target) break
            queue.poll()
            now = next.time
            next.action()
            ran = true
        }
        now = target
        return ran
    }
}
