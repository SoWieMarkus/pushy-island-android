package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class Buoy extends Terrain{

    public Buoy(Coordinate coordinate) {
        super(coordinate, R.drawable.buoy);
    }

    @Override
    public int getElevation() {
        return Integer.MAX_VALUE;
    }
}
