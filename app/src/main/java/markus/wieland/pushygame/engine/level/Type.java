package markus.wieland.pushygame.engine.level;

import androidx.annotation.DrawableRes;

public interface Type {

    String getValue();

    @DrawableRes
    int getDrawable();

    static Type getByValue(String value, boolean isTerrain) {
        Type[] types = isTerrain ? TerrainType.class.getEnumConstants() : EntityType.class.getEnumConstants();
        if (types == null) throw new IllegalStateException();
        for (Type type : types) {
            if (value.equals(type.getValue())) {
                return type;
            }
        }
        throw new IllegalArgumentException("This type is not known!" + value + " " + isTerrain);
    }

    static String addRedundantZeros(String binary, int amount){
        if (binary.length() > amount) {
            throw new IllegalStateException();
        }
        StringBuilder binaryBuilder = new StringBuilder(binary);
        while (binaryBuilder.length() < amount) {
            binaryBuilder.insert(0, "0");
        }
        binary = binaryBuilder.toString();
        return binary;
    }
}
