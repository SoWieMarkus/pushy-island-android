package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class ChangeableFlower extends Sand {

    public ChangeableFlower(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    public void change() {
        if (getType() == TerrainType.CHANGEABLE_FLOWER_GREEN) {
            setType(TerrainType.CHANGEABLE_FLOWER_RED);
            return;
        }
        setType(TerrainType.CHANGEABLE_FLOWER_GREEN);
    }
}
