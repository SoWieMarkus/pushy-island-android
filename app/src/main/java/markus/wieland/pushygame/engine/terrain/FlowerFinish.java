package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;

public class FlowerFinish extends Grass {

    public FlowerFinish(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    @Override
    public int getElevation() {
        return ELEVATION_GRASS;
    }


    public EntityType getOpposite() {
        return getType() == TerrainType.FLOWER_RED_PRESSURE_PLATE ? EntityType.FLOWER_YELLOW : EntityType.FLOWER_RED;
    }

    public EntityType getFlowerType() {
        return getType() == TerrainType.FLOWER_RED_PRESSURE_PLATE ? EntityType.FLOWER_RED : EntityType.FLOWER_YELLOW;
    }
}
