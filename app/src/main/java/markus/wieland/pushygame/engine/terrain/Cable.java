package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Cable extends Sand{

    private boolean active;

    public Cable(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public int getDrawable() {
        return active ? R.drawable.cable_active : super.getDrawable();
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
