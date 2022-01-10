package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class SetTask extends Task{

    private final Coordinate coordinate;
    private final Type currentTypeOfTheField;
    private final Type typeWhichWillBeSet;

    public SetTask(LevelBuilder levelBuilder, Coordinate coordinate, Type typeWhichWillBeSet) {
        super(levelBuilder);
        this.currentTypeOfTheField = getLevelBuilder().getCurrentTypeOfField(coordinate, typeWhichWillBeSet);
        this.coordinate = coordinate;
        this.typeWhichWillBeSet = typeWhichWillBeSet;
    }

    @Override
    public void execute() {
        getLevelBuilder().setTypeOfField(coordinate, typeWhichWillBeSet);
    }

    @Override
    public void undo() {
        getLevelBuilder().setTypeOfField(coordinate, currentTypeOfTheField);
    }
}
