package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Boat extends Terrain{

    public Boat(Coordinate coordinate) {
        super(coordinate, R.drawable.boat);
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }

    @Override
    public TerrainType getType() {
        return TerrainType.BOAT;
    }
}
