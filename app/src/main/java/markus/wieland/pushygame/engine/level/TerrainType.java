package markus.wieland.pushygame.engine.level;

import androidx.annotation.DrawableRes;

import com.google.gson.annotations.SerializedName;

import markus.wieland.pushygame.R;

public enum TerrainType implements Type {

    @SerializedName("water") WATER(0, R.drawable.water, 1),
    @SerializedName("box_water") BOX_WATER(1, R.drawable.box_water, 2),

    @SerializedName("sand") SAND(2, R.drawable.sand, 1),
    @SerializedName("sand_top_left") SAND_TOP_LEFT(3, R.drawable.sand_top_left,1),
    @SerializedName("sand_top_right") SAND_TOP_RIGHT(4, R.drawable.sand_top_right, 1),
    @SerializedName("sand_bottom_left") SAND_BOTTOM_LEFT(5, R.drawable.sand_bottom_left, 1),
    @SerializedName("sand_bottom_right") SAND_BOTTOM_RIGHT(6, R.drawable.sand_bottom_right, 1),

    @SerializedName("grass") GRASS(7, R.drawable.grass, 1),
    @SerializedName("grass_top_left") GRASS_TOP_LEFT(8, R.drawable.grass_top_left, 1),
    @SerializedName("grass_top_right") GRASS_TOP_RIGHT(9, R.drawable.grass_top_right, 1),
    @SerializedName("grass_bottom_left") GRASS_BOTTOM_LEFT(10, R.drawable.grass_bottom_left, 1),
    @SerializedName("grass_bottom_right") GRASS_BOTTOM_RIGHT(11, R.drawable.grass_bottom_right, 1),

    @SerializedName("spring") SPRING(12, R.drawable.spring, 9),
    @SerializedName("sand_with_water") WATER_HOLE(13, R.drawable.sand_with_water, 9),
    @SerializedName("sand_with_farm") FARM(14, R.drawable.sand_with_farm, 9),

    @SerializedName("statue_finish_red") STATUE_FINISH_RED(15, R.drawable.statue_finish_red, 13),
    @SerializedName("statue_finish_green") STATUE_FINISH_GREEN(17, R.drawable.statue_finish_green, 14),
    @SerializedName("statue_finish_blue") STATUE_FINISH_BLUE(16, R.drawable.statue_finish_blue, 15),

    @SerializedName("bomb_field") BOMB_FIELD(18, R.drawable.bomb_field, 18),
    @SerializedName("hole") HOLE(19, R.drawable.hole, 27),

    @SerializedName("spike_pressure_plate") SPIKE_PRESSURE_PLATE(20, R.drawable.spike_pressure_plate, 36),
    @SerializedName("spikes") SPIKES(21, R.drawable.spikes,36),

    @SerializedName("boat") BOAT(22, R.drawable.boat, 44),
    @SerializedName("teleporter") TELEPORTER(23, R.drawable.teleporter, 2, 40),

    @SerializedName("water_invisible_pressure_plate") WATER_INVISIBLE_PRESSURE_PLATE(25, R.drawable.water_invisible_pressure_plate, 86),
    @SerializedName("water_invisible") WATER_INVISIBLE(26, R.drawable.water_invisible, 86),

    @SerializedName("coconut_tunnel") COCONUT_TUNNEL(27, R.drawable.coconut_tunnel, 96),
    @SerializedName("coconut_tunnel_finish") COCONUT_TUNNEL_FINISH(28, R.drawable.coconut_tunnel_finish, 96),

    @SerializedName("item_teleporter") ITEM_TELEPORTER(29, R.drawable.item_teleporter, 2, 66),
    @SerializedName("ice") ICE(30, R.drawable.ice, 76),

    @SerializedName("flower_red_pressure_plate") FLOWER_RED_PRESSURE_PLATE(31, R.drawable.flower_red_pressure_plate, 110),
    @SerializedName("flower_pressure_plate") FLOWER_YELLOW_PRESSURE_PLATE(32, R.drawable.flower_pressure_plate, 105),

    @SerializedName("string_pressure_plate") STRING_PRESSURE_PLATE(33, R.drawable.string_pressure_plate, 130),
    @SerializedName("changable_flower_red") CHANGEABLE_FLOWER_RED(34, R.drawable.changable_flower_red, 130),
    @SerializedName("changable_flower_green") CHANGEABLE_FLOWER_GREEN(35, R.drawable.changable_flower_green, 130),

    @SerializedName("barrel_finish") BARREL_FINISH(36, R.drawable.barrel_finish, 148),

    @SerializedName("cable") CABLE(37, R.drawable.cable, true, 176),
    @SerializedName("logic_pressure_plate") LOGIC_PRESSURE_PLATE(38, R.drawable.logic_pressure_plate, true, 176),
    @SerializedName("door") DOOR(39, R.drawable.door, true, 176);

    private final int value;

    @DrawableRes
    private final int drawable;

    static final int AMOUNT_BITS = 6;

    private final int allowedInstances;

    private final boolean isLogicPart;

    private final int firstAppearance;

    TerrainType(int value, int drawable, int allowedInstances, int firstAppearance) {
        this.value = value;
        this.drawable = drawable;
        this.allowedInstances = allowedInstances;
        this.isLogicPart = false;
        this.firstAppearance = firstAppearance;
    }

    TerrainType(int value, @DrawableRes int drawable, boolean isLogicPart, int firstAppearance) {
        this.value = value;
        this.drawable = drawable;
        this.allowedInstances = Type.UNLIMITED;
        this.isLogicPart = isLogicPart;
        this.firstAppearance = firstAppearance;
    }

    TerrainType(int value, @DrawableRes int drawable, int firstAppearance) {
        this.value = value;
        this.drawable = drawable;
        this.allowedInstances = Type.UNLIMITED;
        this.isLogicPart = false;
        this.firstAppearance = firstAppearance;
    }


    @Override
    public String getValue() {
        return Type.addRedundantZeros(Integer.toBinaryString(value), AMOUNT_BITS);
    }

    @Override
    public boolean isLogicPart() {
        return isLogicPart;
    }

    @Override
    public int firstAppearance() {
        return firstAppearance;
    }

    @Override
    public int getDrawable() {
        return drawable;
    }

    @Override
    public int getAmountOfAllowedInstances() {
        return allowedInstances;
    }
}
