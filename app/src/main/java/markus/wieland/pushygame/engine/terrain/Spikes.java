package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.terrain.pressure.PressurePlateTerrain;

public class Spikes extends Sand {
    private boolean isActive;

    public Spikes(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
        isActive = true;
    }

    @Override
    public int getDrawable() {
        return isActive ? super.getDrawable() : R.drawable.spikes_deactivated;
    }

    @Override
    public int getElevation() {
        return isActive ? Integer.MAX_VALUE : ELEVATION_SAND;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean destroysFlyingStone() {
        return isActive();
    }

    public void setActive(boolean active, Entity entity) {
        isActive = entity == null && active;
    }
}
