package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class BarrelFinish extends Terrain {

    public BarrelFinish(Coordinate coordinate) {
        super(coordinate, R.drawable.barrel_finish);
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }

    @Override
    public TerrainType getType() {
        return TerrainType.BARREL_FINISH;
    }
}
