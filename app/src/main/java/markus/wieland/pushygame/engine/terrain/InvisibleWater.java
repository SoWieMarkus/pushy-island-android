package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class InvisibleWater extends Terrain {

    private boolean isVisible;

    public InvisibleWater(Coordinate coordinate) {
        super(coordinate, R.drawable.water);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    @Override
    public int getDrawable() {
        return isVisible ? R.drawable.water_invisible :super.getDrawable();
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }

    @Override
    public boolean coconutCanEnterFromTunnel() {
        return true;
    }
}
