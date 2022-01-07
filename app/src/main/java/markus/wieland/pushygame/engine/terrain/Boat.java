package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class Boat extends Terrain{

    public Boat(Coordinate coordinate) {
        super(coordinate, R.drawable.boat);
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }
}
