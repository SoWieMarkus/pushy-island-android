package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class ChangeableFlower extends Terrain {

    private TerrainType terrainType;

    public ChangeableFlower(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType == TerrainType.CHANGEABLE_FLOWER_GREEN ? R.drawable.changable_flower_green : R.drawable.changable_flower_red);
        this.terrainType = terrainType;
    }

    @Override
    public int getDrawable() {
        return terrainType == TerrainType.CHANGEABLE_FLOWER_GREEN ? R.drawable.changable_flower_green : R.drawable.changable_flower_red;
    }

    public void change() {
        if (terrainType == TerrainType.CHANGEABLE_FLOWER_GREEN) {
            terrainType = TerrainType.CHANGEABLE_FLOWER_RED;
            return;
        }
        terrainType = TerrainType.CHANGEABLE_FLOWER_GREEN;
    }

    @Override
    public int getElevation() {
        return ELEVATION_SAND;
    }

    @Override
    public TerrainType getType() {
        return terrainType;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }
}
