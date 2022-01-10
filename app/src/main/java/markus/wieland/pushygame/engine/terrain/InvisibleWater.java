package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class InvisibleWater extends Terrain {

    private boolean isVisible;

    public InvisibleWater(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    @Override
    public int getDrawable() {
        return isVisible ? R.drawable.water_invisible : super.getDrawable();
    }

    @Override
    public boolean coconutCanEnterFromTunnel() {
        return true;
    }


}
