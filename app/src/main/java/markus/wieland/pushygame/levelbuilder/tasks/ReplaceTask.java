package markus.wieland.pushygame.levelbuilder.tasks;

import java.util.List;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Field;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class ReplaceTask extends MultipleTask {

    private final Type type;
    private final Coordinate coordinate;
    private int allowedInstances;
    private final Type replaceType;

    public ReplaceTask(LevelBuilder levelBuilder, Type type, Coordinate coordinate) {
        super(levelBuilder);
        this.type = type;
        this.allowedInstances = type.getAmountOfAllowedInstances() - 1;
        this.coordinate = coordinate;
        this.replaceType = type instanceof TerrainType ? TerrainType.SAND : EntityType.NO_ENTITY;
    }

    @Override
    public void execute() {
        List<? extends Field> fields = type instanceof TerrainType
                ? getLevelBuilder().getTerrainManager().getOfType(type)
                : getLevelBuilder().getEntityManager().getOfType(type);
        for (Field field : fields) {
            if (allowedInstances > 0) {
                allowedInstances--;
                continue;
            }
            executeSubTask(new SetTask(getLevelBuilder(), field.getCoordinate(), replaceType));
        }
        executeSubTask(new SetTask(getLevelBuilder(), coordinate, type));
    }
}
