package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Field;
import markus.wieland.pushygame.engine.level.TerrainType;

public abstract class Terrain extends Field {

    public static final int ELEVATION_WATER = -1;
    public static final int ELEVATION_SAND = 0;
    public static final int ELEVATION_GRASS = 1;

    public Terrain(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    public int getElevation() {
        return ELEVATION_SAND;
    }

    @Override
    public boolean destroysFlyingStone() {
        return getElevation() > ELEVATION_SAND;
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

    @Override
    public TerrainType getType() {
        return (TerrainType) super.getType();
    }

}
