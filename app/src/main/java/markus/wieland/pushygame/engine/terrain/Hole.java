package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Hole extends Terrain {

    private boolean isFilled;

    public Hole(Coordinate coordinate) {
        super(coordinate, R.drawable.hole);
        isFilled = false;
    }

    public boolean isFilled() {
        return isFilled;
    }

    public void setFilled(){
        this.isFilled = true;
    }

    @Override
    public int getElevation() {
        return isFilled ? ELEVATION_SAND : 1000;
    }

    @Override
    public TerrainType getType() {
        return TerrainType.HOLE;
    }

    @Override
    public int getDrawable() {
        return !isFilled ? R.drawable.hole : R.drawable.hole_filled;
    }
}
