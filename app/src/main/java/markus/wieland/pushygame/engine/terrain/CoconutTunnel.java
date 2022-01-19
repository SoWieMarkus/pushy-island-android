package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class CoconutTunnel extends Sand {

    public CoconutTunnel(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    @Override
    public boolean isCoconutTunnel() {
        return true;
    }

    @Override
    public boolean coconutCanEnterFromTunnel() {
        return true;
    }
}
