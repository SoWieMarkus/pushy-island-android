package markus.wieland.pushygame.engine.model

/**
 * Metadata shared by every placeable tile.
 *
 * - [id] is the stable identifier used in level files (format v2).
 * - [maxInstances] limits how often the tile may appear in a level (null = unlimited).
 * - [isLogic] marks logic parts (cables, gates, …) whose placement refreshes cable visuals in the editor.
 * - [symbol] is the preferred legend character when a level is written to disk.
 * - [v1Code] / [legacyId] describe the tile in the old v1 share code and the old asset JSON; only the
 *   converter for old levels uses them.
 */
sealed interface TileType {
    val id: String
    val sprite: Sprite
    val maxInstances: Int?
    val isLogic: Boolean
    val symbol: Char?
    val v1Code: Int
    val legacyId: String
    val layer: Layer

    enum class Layer { TERRAIN, ENTITY }

    companion object {
        val all: List<TileType> get() = TerrainType.entries + EntityType.entries
        private val byId: Map<String, TileType> by lazy { all.associateBy { it.id } }
        fun fromId(id: String): TileType? = byId[id]
    }
}

enum class TerrainType(
    override val v1Code: Int,
    override val sprite: Sprite,
    override val maxInstances: Int? = null,
    override val isLogic: Boolean = false,
    override val symbol: Char? = null,
    legacyId: String? = null,
) : TileType {
    WATER(0, Sprite.WATER, symbol = '~'),
    BOX_WATER(1, Sprite.BOX_WATER),
    SAND(2, Sprite.SAND, symbol = '-'),
    SAND_TOP_LEFT(3, Sprite.SAND_TOP_LEFT),
    SAND_TOP_RIGHT(4, Sprite.SAND_TOP_RIGHT),
    SAND_BOTTOM_LEFT(5, Sprite.SAND_BOTTOM_LEFT),
    SAND_BOTTOM_RIGHT(6, Sprite.SAND_BOTTOM_RIGHT),
    GRASS(7, Sprite.GRASS, symbol = '='),
    GRASS_TOP_LEFT(8, Sprite.GRASS_TOP_LEFT),
    GRASS_TOP_RIGHT(9, Sprite.GRASS_TOP_RIGHT),
    GRASS_BOTTOM_LEFT(10, Sprite.GRASS_BOTTOM_LEFT),
    GRASS_BOTTOM_RIGHT(11, Sprite.GRASS_BOTTOM_RIGHT),
    SPRING(12, Sprite.SPRING),
    WATER_HOLE(13, Sprite.WATER_HOLE, legacyId = "sand_with_water"),
    FARM(14, Sprite.FARM, legacyId = "sand_with_farm"),
    STATUE_FINISH_RED(15, Sprite.STATUE_FINISH_RED),
    STATUE_FINISH_GREEN(17, Sprite.STATUE_FINISH_GREEN),
    STATUE_FINISH_BLUE(16, Sprite.STATUE_FINISH_BLUE),
    BOMB_FIELD(18, Sprite.BOMB_FIELD),
    HOLE(19, Sprite.HOLE),
    SPIKE_PRESSURE_PLATE(20, Sprite.SPIKE_PRESSURE_PLATE),
    SPIKES(21, Sprite.SPIKES),
    BOAT(22, Sprite.BOAT),
    TELEPORTER(23, Sprite.TELEPORTER, maxInstances = 2),
    WATER_INVISIBLE_PRESSURE_PLATE(25, Sprite.WATER_INVISIBLE_PRESSURE_PLATE),
    WATER_INVISIBLE(26, Sprite.WATER_INVISIBLE),
    COCONUT_TUNNEL(27, Sprite.COCONUT_TUNNEL),
    COCONUT_TUNNEL_FINISH(28, Sprite.COCONUT_TUNNEL_FINISH),
    ITEM_TELEPORTER(29, Sprite.ITEM_TELEPORTER, maxInstances = 2),
    ICE(30, Sprite.ICE),
    FLOWER_RED_PRESSURE_PLATE(31, Sprite.FLOWER_RED_PRESSURE_PLATE),
    FLOWER_YELLOW_PRESSURE_PLATE(32, Sprite.FLOWER_YELLOW_PRESSURE_PLATE, legacyId = "flower_pressure_plate"),
    STRING_PRESSURE_PLATE(33, Sprite.STRING_PRESSURE_PLATE),
    CHANGEABLE_FLOWER_RED(34, Sprite.CHANGEABLE_FLOWER_RED),
    CHANGEABLE_FLOWER_GREEN(35, Sprite.CHANGEABLE_FLOWER_GREEN),
    BARREL_FINISH(36, Sprite.BARREL_FINISH),
    CABLE(37, Sprite.CABLE, isLogic = true, symbol = '+'),
    LOGIC_PRESSURE_PLATE(38, Sprite.LOGIC_PRESSURE_PLATE, isLogic = true),
    DOOR(39, Sprite.DOOR, isLogic = true);

    override val id: String = name.lowercase()
    override val legacyId: String = legacyId ?: id
    override val layer get() = TileType.Layer.TERRAIN
}

enum class EntityType(
    override val v1Code: Int,
    override val sprite: Sprite,
    override val maxInstances: Int? = null,
    override val isLogic: Boolean = false,
    override val symbol: Char? = null,
    legacyId: String? = null,
) : TileType {
    TREE(0, Sprite.TREE, symbol = 'T'),
    BOX(1, Sprite.BOX, symbol = 'B'),
    PUSHY(2, Sprite.PUSHY, maxInstances = 1, symbol = 'P'),
    FINISH(3, Sprite.FINISH, maxInstances = 1, symbol = 'F'),
    STONE(4, Sprite.STONE, symbol = 'o'),
    SEA_STAR(6, Sprite.SEA_STAR),
    SEED(7, Sprite.SEED),
    BOTTLE(8, Sprite.BOTTLE),
    STATUE_RED(9, Sprite.STATUE_RED),
    STATUE_GREEN(11, Sprite.STATUE_GREEN),
    STATUE_BLUE(10, Sprite.STATUE_BLUE),
    BOMB(12, Sprite.BOMB),
    CRAB_BABY(13, Sprite.CRAB_BABY),
    CRAB_MOTHER(14, Sprite.CRAB_MOTHER, maxInstances = 1),
    SLINGSHOT(15, Sprite.SLINGSHOT),
    SHOT(16, Sprite.SHOT),
    COCONUT(17, Sprite.COCONUT),
    LEAF_DOWN(18, Sprite.LEAF_DOWN),
    LEAF_UP(19, Sprite.LEAF_UP),
    LEAF_LEFT(20, Sprite.LEAF_LEFT),
    LEAF_RIGHT(21, Sprite.LEAF_RIGHT),
    PIRATE(22, Sprite.PIRATE, maxInstances = 1),
    PIRATE_HUT(23, Sprite.PIRATE_HUT, maxInstances = 1),
    BUOY(24, Sprite.BUOY),
    CHEST(25, Sprite.CHEST),
    COIN(26, Sprite.COIN),
    KEY(27, Sprite.KEY),
    TOWER(28, Sprite.TOWER),
    PEARL(29, Sprite.PEARL),
    SHELL(30, Sprite.SHELL),
    SHELL_OPEN_WITH_PEARL(31, Sprite.SHELL_OPEN_WITH_PEARL),
    COUNT_ONE(32, Sprite.COUNT_ONE, maxInstances = 1),
    COUNT_TWO(33, Sprite.COUNT_TWO, maxInstances = 1),
    COUNT_THREE(34, Sprite.COUNT_THREE, maxInstances = 1),
    COUNT_FOUR(35, Sprite.COUNT_FOUR, maxInstances = 1),
    COUNT_FIVE(36, Sprite.COUNT_FIVE, maxInstances = 1),
    FLOWER_YELLOW(37, Sprite.FLOWER_YELLOW),
    FLOWER_RED(38, Sprite.FLOWER_RED),
    SHELL_1(39, Sprite.SHELL_1),
    SHELL_2(40, Sprite.SHELL_2),
    SHELL_3(41, Sprite.SHELL_3),
    OCTOPUS(42, Sprite.OCTOPUS),
    BARREL(43, Sprite.BARREL),
    LEAF_CHANGER_EAST(44, Sprite.LEAF_CHANGER_EAST),
    LEAF_CHANGER_WEST(45, Sprite.LEAF_CHANGER_WEST),
    LEAF_CHANGER_NORTH(46, Sprite.LEAF_CHANGER_NORTH),
    LEAF_CHANGER_SOUTH(47, Sprite.LEAF_CHANGER_SOUTH),
    LOGIC_GATE_AND(48, Sprite.LOGIC_GATE_AND, isLogic = true),
    LOGIC_GATE_OR(49, Sprite.LOGIC_GATE_OR, isLogic = true),
    LOGIC_GATE_XOR(50, Sprite.LOGIC_GATE_XOR, isLogic = true),
    LAMP(51, Sprite.LAMP, isLogic = true, legacyId = "logic_gate_lamp"),
    LEVER(52, Sprite.LEVER, isLogic = true),
    POWER_BLOCK(53, Sprite.POWER_BLOCK, isLogic = true),
    LOGIC_GATE_NOT(54, Sprite.LOGIC_GATE_NOT, isLogic = true),
    ENERGY(55, Sprite.ENERGY),
    BUTTON(56, Sprite.BUTTON, isLogic = true),
    COUNT_DOWN(57, Sprite.COUNT_DOWN_THREE, isLogic = true),
    REPEATER_NORTH(58, Sprite.REPEATER_NORTH_ACTIVE, isLogic = true),
    REPEATER_SOUTH(59, Sprite.REPEATER_SOUTH_ACTIVE, isLogic = true),
    REPEATER_WEST(60, Sprite.REPEATER_WEST_ACTIVE, isLogic = true),
    REPEATER_EAST(61, Sprite.REPEATER_EAST_ACTIVE, isLogic = true);

    override val id: String = name.lowercase()
    override val legacyId: String = legacyId ?: id
    override val layer get() = TileType.Layer.ENTITY

    companion object {
        /** v1 code of an empty entity cell. */
        const val V1_NO_ENTITY = 5
        const val LEGACY_NO_ENTITY = "no_entity"
    }
}
