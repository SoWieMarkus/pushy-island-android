package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class WaterHole extends Terrain {

    public WaterHole(Coordinate coordinate) {
        super(coordinate, R.drawable.sand_with_water);
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }
}
