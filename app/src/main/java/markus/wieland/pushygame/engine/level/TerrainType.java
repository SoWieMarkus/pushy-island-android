package markus.wieland.pushygame.engine.level;

import androidx.annotation.DrawableRes;

import com.google.gson.annotations.SerializedName;

import markus.wieland.pushygame.R;

public enum TerrainType implements Type {

    @SerializedName("water") WATER(0, R.drawable.water),
    @SerializedName("box_water") BOX_WATER(1, R.drawable.box_water),

    @SerializedName("sand") SAND(2, R.drawable.sand),
    @SerializedName("sand_top_left") SAND_TOP_LEFT(3, R.drawable.sand_top_left),
    @SerializedName("sand_top_right") SAND_TOP_RIGHT(4, R.drawable.sand_top_right),
    @SerializedName("sand_bottom_left") SAND_BOTTOM_LEFT(5, R.drawable.sand_bottom_left),
    @SerializedName("sand_bottom_right") SAND_BOTTOM_RIGHT(6, R.drawable.sand_bottom_right),

    @SerializedName("grass") GRASS(7, R.drawable.grass),
    @SerializedName("grass_top_left") GRASS_TOP_LEFT(8, R.drawable.grass_top_left),
    @SerializedName("grass_top_right") GRASS_TOP_RIGHT(9, R.drawable.grass_top_right),
    @SerializedName("grass_bottom_left") GRASS_BOTTOM_LEFT(10, R.drawable.grass_bottom_left),
    @SerializedName("grass_bottom_right") GRASS_BOTTOM_RIGHT(11, R.drawable.grass_bottom_right),

    @SerializedName("spring") SPRING(12, R.drawable.spring),
    @SerializedName("sand_with_water") WATER_HOLE(13, R.drawable.sand_with_water),
    @SerializedName("sand_with_farm") FARM(14, R.drawable.sand_with_farm),

    @SerializedName("statue_finish_red") STATUE_FINISH_RED(15, R.drawable.statue_finish_red),
    @SerializedName("statue_finish_green") STATUE_FINISH_GREEN(17, R.drawable.statue_finish_green),
    @SerializedName("statue_finish_blue") STATUE_FINISH_BLUE(16, R.drawable.statue_finish_blue),

    @SerializedName("bomb_field") BOMB_FIELD(18, R.drawable.bomb_field),
    @SerializedName("hole") HOLE(19, R.drawable.hole),

    @SerializedName("spike_pressure_plate") SPIKE_PRESSURE_PLATE(20, R.drawable.spike_pressure_plate),
    @SerializedName("spikes") SPIKES(21, R.drawable.spikes),

    @SerializedName("boat") BOAT(22, R.drawable.boat),
    @SerializedName("teleporter") TELEPORTER(23, R.drawable.teleporter, 2),
    @SerializedName("buoy") BUOY(24, R.drawable.buoy),

    @SerializedName("water_invisible_pressure_plate") WATER_INVISIBLE_PRESSURE_PLATE(25, R.drawable.water_invisible_pressure_plate),
    @SerializedName("water_invisible") WATER_INVISIBLE(26, R.drawable.water_invisible),

    @SerializedName("coconut_tunnel") COCONUT_TUNNEL(27, R.drawable.coconut_tunnel),
    @SerializedName("coconut_tunnel_finish") COCONUT_TUNNEL_FINISH(28, R.drawable.coconut_tunnel_finish),

    @SerializedName("item_teleporter") ITEM_TELEPORTER(29, R.drawable.item_teleporter, 2),
    @SerializedName("ice") ICE(30, R.drawable.ice),

    @SerializedName("flower_red_pressure_plate") FLOWER_RED_PRESSURE_PLATE(31, R.drawable.flower_red_pressure_plate),
    @SerializedName("flower_pressure_plate") FLOWER_YELLOW_PRESSURE_PLATE(32, R.drawable.flower_pressure_plate),

    @SerializedName("string_pressure_plate") STRING_PRESSURE_PLATE(33, R.drawable.string_pressure_plate),
    @SerializedName("changable_flower_red") CHANGEABLE_FLOWER_RED(34, R.drawable.changable_flower_red),
    @SerializedName("changable_flower_green") CHANGEABLE_FLOWER_GREEN(35, R.drawable.changable_flower_green),

    @SerializedName("barrel_finish") BARREL_FINISH(36, R.drawable.barrel_finish),

    @SerializedName("barrel_finish") CABLE(37, R.drawable.cable),
    @SerializedName("barrel_finish") LOGIC_PRESSURE_PLATE(38, R.drawable.logic_pressure_plate, true),
    @SerializedName("barrel_finish") DOOR(39, R.drawable.door, true);

    private final int value;

    @DrawableRes
    private final int drawable;

    static final int AMOUNT_BITS = 6;

    private final int allowedInstances;

    private final boolean isLogicPart;

    TerrainType(int value, int drawable, int allowedInstances) {
        this.value = value;
        this.drawable = drawable;
        this.allowedInstances = allowedInstances;
        this.isLogicPart = false;
    }

    TerrainType(int value, @DrawableRes int drawable, boolean isLogicPart) {
        this.value = value;
        this.drawable = drawable;
        this.allowedInstances = Type.UNLIMITED;
        this.isLogicPart = isLogicPart;
    }

    TerrainType(int value, @DrawableRes int drawable) {
        this.value = value;
        this.drawable = drawable;
        this.allowedInstances = Type.UNLIMITED;
        this.isLogicPart = false;
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
    public int getDrawable() {
        return drawable;
    }

    @Override
    public int getAmountOfAllowedInstances() {
        return allowedInstances;
    }
}
