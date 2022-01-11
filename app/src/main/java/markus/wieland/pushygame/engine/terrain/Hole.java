package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Hole extends Sand {

    private boolean isFilled;

    public Hole(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
        isFilled = false;
    }

    public boolean isFilled() {
        return isFilled;
    }

    public void setFilled() {
        this.isFilled = true;
    }

    @Override
    public int getElevation() {
        return isFilled ? ELEVATION_SAND : 1000;
    }

    @Override
    public int getDrawable() {
        return !isFilled ? super.getDrawable() : R.drawable.hole_filled;
    }
}
