package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class BarrelFinish extends Terrain {

    public BarrelFinish(Coordinate coordinate) {
        super(coordinate, R.drawable.barrel_finish);
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }
}
