package markus.wieland.pushygame.engine.model

/** Anything that occupies a cell: a terrain tile or an entity. */
abstract class Field(var coordinate: Coordinate) {

    /** The tile type this field would be saved as (null for things the player can't place). */
    abstract val tileType: TileType?

    /** Images to draw for this field, bottom to top. */
    open val sprites: List<Sprite> get() = listOfNotNull(tileType?.sprite)

    open fun destroysFlyingStone(): Boolean = false
}
