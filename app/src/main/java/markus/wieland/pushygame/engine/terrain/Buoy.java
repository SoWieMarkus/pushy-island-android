package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Buoy extends Terrain{

    public Buoy(Coordinate coordinate) {
        super(coordinate, R.drawable.buoy);
    }

    @Override
    public int getElevation() {
        return Integer.MAX_VALUE;
    }

    @Override
    public TerrainType getType() {
        return TerrainType.BUOY;
    }
}
