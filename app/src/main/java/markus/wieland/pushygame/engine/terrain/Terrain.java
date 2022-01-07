package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Field;

public abstract class Terrain extends Field {

    public static final int ELEVATION_WATER = -1;
    public static final int ELEVATION_SAND = 0;
    public static final int ELEVATION_GRASS = 1;

    public Terrain(Coordinate coordinate, int drawable) {
        super(coordinate, drawable);
    }

    public abstract int getElevation();

    @Override
    public boolean destroysFlyingStone() {
        return getElevation() > ELEVATION_SAND;
    }

    public boolean isPossibleToMove(Terrain terrain) {
        if (terrain == null) throw new IllegalArgumentException("Terrain should not be null.");
        if (getElevation() == ELEVATION_WATER || terrain.getElevation() == ELEVATION_WATER) return false;
        return getElevation() >= terrain.getElevation();
    }

    public boolean hasSameElevationAs(Terrain terrain) {
        if (terrain == null) throw new IllegalArgumentException("Terrain should not be null.");
        return getElevation() == terrain.getElevation();
    }

    public boolean coconutCanEnterFromTunnel() {
        return false;
    }

    public boolean isCoconutTunnel() {
        return false;
    }

}
