package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Water extends Terrain {

    public Water(Coordinate coordinate) {
        super(coordinate, R.drawable.water);
    }

    @Override
    public int getElevation() {
        return ELEVATION_WATER;
    }

    @Override
    public TerrainType getType() {
        return TerrainType.WATER;
    }
}
