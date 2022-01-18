package markus.wieland.pushygame.engine.level;

import androidx.annotation.DrawableRes;

import com.google.gson.annotations.SerializedName;

import markus.wieland.pushygame.R;

public enum EntityType implements Type {
    @SerializedName("no_entity") NO_ENTITY(5, R.drawable.no_entity, 1),
    @SerializedName("tree") TREE(0, R.drawable.tree, 1),
    @SerializedName("box") BOX(1, R.drawable.box, 2),
    @SerializedName("pushy") PUSHY(2, R.drawable.pushy, 1, 1),
    @SerializedName("finish") FINISH(3, R.drawable.finish, 1, 1),
    @SerializedName("stone") STONE(4, R.drawable.stone, 1),
    @SerializedName("seastar") SEA_STAR(6, R.drawable.seastar, 7),
    @SerializedName("seed") SEED(7, R.drawable.seed, 9),
    @SerializedName("bottle") BOTTLE(8, R.drawable.bottle, 9),
    @SerializedName("statue_red") STATUE_RED(9, R.drawable.statue_red, 13),
    @SerializedName("statue_green") STATUE_GREEN(11, R.drawable.statue_green, 14),
    @SerializedName("statue_blue") STATUE_BLUE(10, R.drawable.statue_blue, 15),
    @SerializedName("bomb") BOMB(12, R.drawable.bomb, 18),
    @SerializedName("crab_baby") CRAB(13, R.drawable.crab_baby, 22),
    @SerializedName("crab_mother") CRAB_MOTHER(14, R.drawable.crab_mother, 1, 22),
    @SerializedName("slingshot") SLING_SHOT(15, R.drawable.slingshot, 27),
    @SerializedName("shot") SHOT(16, R.drawable.shot, 27),
    @SerializedName("coconut") COCONUT(17, R.drawable.coconut, 27),
    @SerializedName("leaf_down") LEAF_DOWN(18, R.drawable.leaf_down, 32),
    @SerializedName("leaf_up") LEAF_UP(19, R.drawable.leaf_up, 32),
    @SerializedName("leaf_left") LEAF_LEFT(20, R.drawable.leaf_left, 32),
    @SerializedName("leaf_right") LEAF_RIGHT(21, R.drawable.leaf_right, 32),
    @SerializedName("pirat") PIRATE(22, R.drawable.pirat, 1, 44),
    @SerializedName("pirat_hut") PIRATE_HUT(23, R.drawable.pirat_hut, 1, 44),
    @SerializedName("buoy") BUOY(24, R.drawable.buoy, 44),
    @SerializedName("chest") CHEST(25, R.drawable.chest, 50),
    @SerializedName("coin") COIN(26, R.drawable.coin, 44),
    @SerializedName("key") KEY(27, R.drawable.key, 50),
    @SerializedName("tower") TOWER(28, R.drawable.tower, 54),
    @SerializedName("pearl") PEARL(29, R.drawable.pearl, 54),
    @SerializedName("shell") SHELL(30, R.drawable.shell, 57),
    @SerializedName("shell_open_with_pearl") SHELL_OPEN_WITH_PEARL(31, R.drawable.shell_open_with_pearl, 57),
    @SerializedName("count_one") COUNT_ONE(32, R.drawable.count_one, 1, 71),
    @SerializedName("count_two") COUNT_TWO(33, R.drawable.count_two, 1, 71),
    @SerializedName("count_three") COUNT_THREE(34, R.drawable.count_three, 1, 71),
    @SerializedName("count_four") COUNT_FOUR(35, R.drawable.count_four, 1, 71),
    @SerializedName("count_five") COUNT_FIVE(36, R.drawable.count_five, 1, 71),
    @SerializedName("flower") FLOWER_YELLOW(37, R.drawable.flower, 105),
    @SerializedName("flower_red") FLOWER_RED(38, R.drawable.flower_red, 110),
    @SerializedName("shell_1") SHELL_1(39, R.drawable.shell_1, 119),
    @SerializedName("shell_2") SHELL_2(40, R.drawable.shell_2, 119),
    @SerializedName("shell_3") SHELL_3(41, R.drawable.shell_3, 119),
    @SerializedName("octopus") OCTOPUS(42, R.drawable.octopus, 141),
    @SerializedName("barrel") BARREL(43, R.drawable.barrel, 148),
    @SerializedName("leaf_changer_east") LEAF_CHANGER_EAST(44, R.drawable.leaf_changer_east, 155),
    @SerializedName("leaf_changer_west") LEAF_CHANGER_WEST(45, R.drawable.leaf_changer_west, 155),
    @SerializedName("leaf_changer_north") LEAF_CHANGER_NORTH(46, R.drawable.leaf_changer_north, 155),
    @SerializedName("leaf_changer_south") LEAF_CHANGER_SOUTH(47, R.drawable.leaf_changer_south, 155),
    @SerializedName("logic_gate_and") LOGIC_GATE_AND(48, R.drawable.logic_gate_and,true, 176),
    @SerializedName("logic_gate_or") LOGIC_GATE_OR(49, R.drawable.logic_gate_or,true, 176),
    @SerializedName("logic_gate_xor") LOGIC_GATE_XOR(50, R.drawable.logic_gate_xor, true, 176),
    @SerializedName("logic_gate_lamp") LAMP(51, R.drawable.lamp, true, 176),
    @SerializedName("lever") LEVER(52, R.drawable.lever, true, 176),
    @SerializedName("power_block") POWER_BLOCK(53, R.drawable.power_block, true, 176),
    @SerializedName("logic_gate_not") LOGIC_GATE_NOT(54, R.drawable.logic_gate_not, true, 176),
    @SerializedName("energy") ENERGY(55, R.drawable.energy, 176),
    @SerializedName("button") BUTTON(56, R.drawable.button, true, 176),
    @SerializedName("count_down") COUNT_DOWN(57, R.drawable.count_down_three, true, 176),
    @SerializedName("repeater_north") REPEATER_NORTH(58, R.drawable.repeater_north_active, true, 176),
    @SerializedName("repeater_south") REPEATER_SOUTH(59, R.drawable.repeater_south_active, true, 176),
    @SerializedName("repeater_west") REPEATER_WEST(60, R.drawable.repeater_west_active, true, 176),
    @SerializedName("repeater_east") REPEATER_EAST(61, R.drawable.repeater_east_active, true, 176);

    private final int value;

    @DrawableRes
    private final int drawable;

    private final int allowedInstances;

    static final int AMOUNT_BITS = 6;

    private final boolean isLogicPart;

    private final int firstAppearance;

    EntityType(int value, int drawable, int allowedInstances, int firstAppearance) {
        this.value = value;
        this.drawable = drawable;
        this.allowedInstances = allowedInstances;
        this.isLogicPart = false;
        this.firstAppearance = firstAppearance;
    }

    EntityType(int value, @DrawableRes int drawable, boolean isLogicPart, int firstAppearance) {
        this.value = value;
        this.drawable = drawable;
        this.allowedInstances = Type.UNLIMITED;
        this.isLogicPart = isLogicPart;
        this.firstAppearance = firstAppearance;
    }

    EntityType(int value, @DrawableRes int drawable, int firstAppearance) {
        this.value = value;
        this.drawable = drawable;
        this.allowedInstances = Type.UNLIMITED;
        this.isLogicPart = false;
        this.firstAppearance = firstAppearance;
    }

    @Override
    public String getValue() {
        String binaryString = Integer.toBinaryString(value);
        return Type.addRedundantZeros(binaryString, AMOUNT_BITS);
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
