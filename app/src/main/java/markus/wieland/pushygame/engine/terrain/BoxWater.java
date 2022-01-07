package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class BoxWater extends Terrain{

    public BoxWater(Coordinate coordinate) {
        super(coordinate, R.drawable.box_water);
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }
}
