package markus.wieland.pushygame.engine.level;

import androidx.annotation.DrawableRes;

import com.google.gson.annotations.SerializedName;

import markus.wieland.pushygame.R;

public enum EntityType implements Type {
    @SerializedName("no_entity") NO_ENTITY(5, R.drawable.no_entity),
    @SerializedName("tree") TREE(0, R.drawable.tree),
    @SerializedName("box") BOX(1, R.drawable.box),
    @SerializedName("pushy") PUSHY(2, R.drawable.pushy),
    @SerializedName("finish") FINISH(3, R.drawable.finish),
    @SerializedName("stone") STONE(4, R.drawable.stone),
    @SerializedName("seastar") SEA_STAR(6, R.drawable.seastar),
    @SerializedName("seed") SEED(7, R.drawable.seed),
    @SerializedName("bottle") BOTTLE(8, R.drawable.bottle),
    @SerializedName("statue_red") STATUE_RED(9, R.drawable.statue_red),
    @SerializedName("statue_green") STATUE_GREEN(11, R.drawable.statue_green),
    @SerializedName("statue_blue") STATUE_BLUE(10, R.drawable.statue_blue),
    @SerializedName("bomb") BOMB(12, R.drawable.bomb),
    @SerializedName("crab_baby") CRAB(13, R.drawable.crab_baby),
    @SerializedName("crab_mother") CRAB_MOTHER(14, R.drawable.crab_mother),
    @SerializedName("slingshot") SLING_SHOT(15, R.drawable.slingshot),
    @SerializedName("shot") SHOT(16, R.drawable.shot),
    @SerializedName("coconut") COCONUT(17, R.drawable.coconut),
    @SerializedName("leaf_down") LEAF_DOWN(18, R.drawable.leaf_down),
    @SerializedName("leaf_up") LEAF_UP(19, R.drawable.leaf_up),
    @SerializedName("leaf_left") LEAF_LEFT(20, R.drawable.leaf_left),
    @SerializedName("leaf_right") LEAF_RIGHT(21, R.drawable.leaf_right),
    @SerializedName("pirat") PIRATE(22, R.drawable.pirat),
    @SerializedName("pirat_hut") PIRATE_HUT(23, R.drawable.pirat_hut),
    @SerializedName("chest") CHEST(24, R.drawable.chest),
    @SerializedName("coin") COIN(25, R.drawable.coin),
    @SerializedName("key") KEY(26, R.drawable.key),
    @SerializedName("tower") TOWER(27, R.drawable.tower),
    @SerializedName("pearl") PEARL(28, R.drawable.pearl),
    @SerializedName("shell") SHELL(29, R.drawable.shell),
    @SerializedName("shell_open_with_pearl") SHELL_OPEN_WITH_PEARL(30, R.drawable.shell_open_with_pearl),
    @SerializedName("count_one") COUNT_ONE(31, R.drawable.count_one),
    @SerializedName("count_two") COUNT_TWO(32, R.drawable.count_two),
    @SerializedName("count_three") COUNT_THREE(33, R.drawable.count_three),
    @SerializedName("count_four") COUNT_FOUR(34, R.drawable.count_four),
    @SerializedName("count_five") COUNT_FIVE(35, R.drawable.count_five),
    @SerializedName("flower") FLOWER_YELLOW(36, R.drawable.flower),
    @SerializedName("flower_red") FLOWER_RED(37, R.drawable.flower_red),
    @SerializedName("shell_1") SHELL_1(38, R.drawable.shell_1),
    @SerializedName("shell_2") SHELL_2(39, R.drawable.shell_2),
    @SerializedName("shell_3") SHELL_3(40, R.drawable.shell_3),
    @SerializedName("octopus") OCTOPUS(41, R.drawable.octopus),
    @SerializedName("barrel") BARREL(42, R.drawable.barrel),
    @SerializedName("leaf_changer_east") LEAF_CHANGER_EAST(43, R.drawable.leaf_changer_east),
    @SerializedName("leaf_changer_west") LEAF_CHANGER_WEST(44, R.drawable.leaf_changer_west),
    @SerializedName("leaf_changer_north") LEAF_CHANGER_NORTH(45, R.drawable.leaf_changer_north),
    @SerializedName("leaf_changer_south") LEAF_CHANGER_SOUTH(46, R.drawable.leaf_changer_south);

    private final int value;

    @DrawableRes
    private final int drawable;

    static final int AMOUNT_BITS = 6;

    EntityType(int value, @DrawableRes int drawable) {
        this.value = value;
        this.drawable = drawable;
    }

    @Override
    public String getValue() {
        String binaryString = Integer.toBinaryString(value);
        return Type.addRedundantZeros(binaryString, AMOUNT_BITS);
    }

    @Override
    public int getDrawable() {
        return drawable;
    }
}
