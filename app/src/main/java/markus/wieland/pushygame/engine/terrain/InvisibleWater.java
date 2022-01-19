package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class InvisibleWater extends Terrain {

    private boolean isNotVisible;

    public InvisibleWater(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
        isNotVisible = false;
    }

    public void setVisible(boolean visible) {
        isNotVisible = visible;
    }

    @Override
    public int getDrawable() {
        return isNotVisible ? R.drawable.water : super.getDrawable();
    }

    @Override
    public boolean coconutCanEnterFromTunnel() {
        return true;
    }

}
