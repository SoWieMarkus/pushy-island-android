package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class Water extends Terrain {

    public Water(Coordinate coordinate) {
        super(coordinate, R.drawable.water);
    }

    @Override
    public int getElevation() {
        return ELEVATION_WATER;
    }
}
