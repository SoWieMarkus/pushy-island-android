package markus.wieland.pushygame.engine

import markus.wieland.pushygame.engine.entity.Collectible
import markus.wieland.pushygame.engine.model.Sprite
import kotlin.reflect.KClass

data class InventoryItem(val sprite: Sprite, val count: Int)

class Inventory {
    private val items = LinkedHashMap<KClass<out Collectible>, MutableList<Collectible>>()

    fun add(item: Collectible) {
        items.getOrPut(item::class) { mutableListOf() }.add(item)
    }

    fun amount(type: KClass<out Collectible>): Int = items[type]?.size ?: 0

    /** Removes [count] items of [type] if there are enough of them. */
    fun take(type: KClass<out Collectible>, count: Int): Boolean {
        val list = items[type] ?: return false
        if (list.size < count) return false
        repeat(count) { list.removeAt(0) }
        return true
    }

    fun snapshot(): List<InventoryItem> = items.values
        .filter { it.isNotEmpty() }
        .map { InventoryItem(it.first().sprites.first(), it.size) }
}
