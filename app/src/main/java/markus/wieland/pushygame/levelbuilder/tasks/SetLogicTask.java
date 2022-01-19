package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.engine.terrain.Cable;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class SetLogicTask extends MultipleTask {

    private final Coordinate coordinate;
    private final Type type;

    public SetLogicTask(LevelBuilder levelBuilder, Coordinate coordinate, Type type) {
        super(levelBuilder);
        this.coordinate = coordinate;
        this.type = type;
    }

    @Override
    public void execute() {
        executeSubTask(new SetTask(getLevelBuilder(), coordinate, type));
        updateCables();
    }

    private void updateCables() {
        for (Cable cable : getLevelBuilder().getTerrainManager().getOfType(Cable.class)) {
            cable.updateDrawables(getLevelBuilder());
        }
    }

    @Override
    public void undo() {
        super.undo();
        updateCables();
    }
}
