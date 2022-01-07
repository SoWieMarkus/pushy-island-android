package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;

public class FlowerFinish extends Terrain {

    private final TerrainType terrainType;

    public FlowerFinish(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType == TerrainType.FLOWER_RED_PRESSURE_PLATE ? R.drawable.flower_red_pressure_plate : R.drawable.flower_pressure_plate);
        this.terrainType = terrainType;
    }

    @Override
    public int getElevation() {
        return ELEVATION_GRASS;
    }

    public EntityType getOpposite(){
        return terrainType == TerrainType.FLOWER_RED_PRESSURE_PLATE ? EntityType.FLOWER_YELLOW : EntityType.FLOWER_RED;
    }

    public EntityType getFlowerType(){
        return terrainType == TerrainType.FLOWER_RED_PRESSURE_PLATE ? EntityType.FLOWER_RED : EntityType.FLOWER_YELLOW;
    }
}
