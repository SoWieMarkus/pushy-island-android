package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class ResetTask extends MultipleTask {

    public ResetTask(LevelBuilder levelBuilder) {
        super(levelBuilder);
    }

    @Override
    public void execute() {
        for (int x = 0; x < LevelBuilder.LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LevelBuilder.LEVEL_WIDTH; y++) {
                Coordinate coordinate = new Coordinate(x, y);
                executeSubTask(new SetTask(getLevelBuilder(), coordinate, TerrainType.WATER));
                executeSubTask(new SetTask(getLevelBuilder(), coordinate, EntityType.NO_ENTITY));
            }
        }
    }

}
