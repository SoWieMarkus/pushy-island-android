package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class CoconutTunnel extends Terrain{

    public CoconutTunnel(Coordinate coordinate) {
        super(coordinate, R.drawable.coconut_tunnel);
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }

    @Override
    public boolean isCoconutTunnel() {
        return true;
    }

    @Override
    public TerrainType getType() {
        return TerrainType.COCONUT_TUNNEL;
    }

    @Override
    public boolean coconutCanEnterFromTunnel() {
        return true;
    }
}
