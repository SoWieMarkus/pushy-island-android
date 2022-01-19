package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Farm extends Sand {

    private boolean hasSeed;

    public Farm(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
        hasSeed = false;
    }

    @Override
    public int[] getDrawableList() {
        if (!hasSeed) return super.getDrawableList();
        return new int[]{super.getDrawable(), R.drawable.seed};
    }

    public boolean hasSeed() {
        return hasSeed;
    }

    public void setHasSeed(boolean hasSeed) {
        this.hasSeed = hasSeed;
    }

}
