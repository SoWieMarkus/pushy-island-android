package markus.wieland.pushygame.engine.level;

import com.google.gson.annotations.SerializedName;

public enum TerrainType implements Tag {

    @SerializedName("water") WATER,
    @SerializedName("box_water") BOX_WATER,

    @SerializedName("sand") SAND,
    @SerializedName("sand_top_left") SAND_TOP_LEFT,
    @SerializedName("sand_top_right") SAND_TOP_RIGHT,
    @SerializedName("sand_bottom_left") SAND_BOTTOM_LEFT,
    @SerializedName("sand_bottom_right") SAND_BOTTOM_RIGHT,

    @SerializedName("grass") GRASS,
    @SerializedName("grass_top_left") GRASS_TOP_LEFT,
    @SerializedName("grass_top_right") GRASS_TOP_RIGHT,
    @SerializedName("grass_bottom_left") GRASS_BOTTOM_LEFT,
    @SerializedName("grass_bottom_right") GRASS_BOTTOM_RIGHT,

    @SerializedName("spring") SPRING,
    @SerializedName("sand_with_water") WATER_HOLE,
    @SerializedName("sand_with_farm") FARM,

    @SerializedName("statue_finish_red") STATUE_FINISH_RED,
    @SerializedName("statue_finish_blue") STATUE_FINISH_BLUE,
    @SerializedName("statue_finish_green") STATUE_FINISH_GREEN,

    @SerializedName("bomb_field") BOMB_FIELD,
    @SerializedName("hole") HOLE,

    @SerializedName("spike_pressure_plate") SPIKE_PRESSURE_PLATE,
    @SerializedName("spikes") SPIKES,

    @SerializedName("boat") BOAT,
    @SerializedName("teleporter") TELEPORTER,
    @SerializedName("buoy") BUOY,

    @SerializedName("water_invisible_pressure_plate") WATER_INVISIBLE_PRESSURE_PLATE,
    @SerializedName("water_invisible") WATER_INVISIBLE,

    @SerializedName("coconut_tunnel") COCONUT_TUNNEL,
    @SerializedName("coconut_tunnel_finish") COCONUT_TUNNEL_FINISH,

    @SerializedName("item_teleporter") ITEM_TELEPORTER,
    @SerializedName("ice") ICE,

    @SerializedName("flower_red_pressure_plate") FLOWER_RED_PRESSURE_PLATE,
    @SerializedName("flower_pressure_plate") FLOWER_YELLOW_PRESSURE_PLATE,

    @SerializedName("string_pressure_plate") STRING_PRESSURE_PLATE,
    @SerializedName("changable_flower_red") CHANGEABLE_FLOWER_RED,
    @SerializedName("changable_flower_green") CHANGEABLE_FLOWER_GREEN,

    @SerializedName("barrel_finish") BARREL_FINISH,


}
