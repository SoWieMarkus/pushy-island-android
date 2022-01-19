package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class EraseTask extends MultipleTask {

    private final Coordinate coordinate;

    public EraseTask(LevelBuilder levelBuilder, Coordinate coordinate) {
        super(levelBuilder);
        this.coordinate = coordinate;
    }

    @Override
    public void execute() {
        Entity entity = getLevelBuilder().getEntityManager().getObject(coordinate);
        Type typeToSet = entity == null ? TerrainType.WATER : EntityType.NO_ENTITY;
        executeSubTask(new SetTask(getLevelBuilder(), coordinate, typeToSet));
    }

}
