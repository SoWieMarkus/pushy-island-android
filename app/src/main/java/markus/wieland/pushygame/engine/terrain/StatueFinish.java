package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;

public class StatueFinish extends Terrain {

    private final TerrainType terrainType;

    public StatueFinish(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, R.drawable.statue_finish_red);
        this.terrainType = terrainType;
        switch (terrainType) {
            case STATUE_FINISH_BLUE:
                setDrawable(R.drawable.statue_finish_blue);
                break;
            case STATUE_FINISH_GREEN:
                setDrawable(R.drawable.statue_finish_green);
                break;
            default:
                setDrawable(R.drawable.statue_finish_red);
                break;
        }
    }

    public boolean equalsType(EntityType entityType) {
        return (terrainType == TerrainType.STATUE_FINISH_BLUE && entityType == EntityType.STATUE_BLUE) ||
                (terrainType == TerrainType.STATUE_FINISH_RED && entityType == EntityType.STATUE_RED) ||
                (terrainType == TerrainType.STATUE_FINISH_GREEN && entityType == EntityType.STATUE_GREEN);
    }

    @Override
    public int getElevation() {
        return 0;
    }

    @Override
    public TerrainType getType() {
        return terrainType;
    }
}
