package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;

public class StatueFinish extends Terrain {

    public StatueFinish(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);

    }

    public boolean equalsType(EntityType entityType) {
        return (getType() == TerrainType.STATUE_FINISH_BLUE && entityType == EntityType.STATUE_BLUE) ||
                (getType() == TerrainType.STATUE_FINISH_RED && entityType == EntityType.STATUE_RED) ||
                (getType() == TerrainType.STATUE_FINISH_GREEN && entityType == EntityType.STATUE_GREEN);
    }

}
