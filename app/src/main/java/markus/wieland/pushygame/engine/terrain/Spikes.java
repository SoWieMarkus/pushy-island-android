package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.terrain.pressure.PressurePlateTerrain;

public class Spikes extends Terrain {
    private boolean isActive;

    public Spikes(Coordinate coordinate) {
        super(coordinate, R.drawable.spikes);
        isActive = true;
    }

    @Override
    public int getDrawable() {
        return isActive ? R.drawable.spikes : R.drawable.spikes_deactivated;
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
