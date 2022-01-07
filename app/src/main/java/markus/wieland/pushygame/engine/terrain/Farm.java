package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class Farm extends Terrain {

    private boolean hasSeed;

    public Farm(Coordinate coordinate) {
        super(coordinate, R.drawable.sand_with_farm);
        hasSeed = false;
    }

    @Override
    public int getDrawable() {
        return hasSeed ? R.drawable.farm_with_seed : super.getDrawable();
    }

    public boolean hasSeed() {
        return hasSeed;
    }

    public void setHasSeed(boolean hasSeed) {
        this.hasSeed = hasSeed;
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }

}
