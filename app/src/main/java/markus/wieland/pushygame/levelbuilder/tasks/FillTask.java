package markus.wieland.pushygame.levelbuilder.tasks;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class FillTask extends Task {

    private final Coordinate coordinate;
    private final List<Task> tasksExecuted;
    private final Type typeWhichWillBeSet;
    private final Type typeWhichWillBeReplaced;

    public FillTask(LevelBuilder levelBuilder, Coordinate coordinate, Type typeWhichWillBeSet) {
        super(levelBuilder);
        this.coordinate = coordinate;
        this.tasksExecuted = new ArrayList<>();
        this.typeWhichWillBeSet = typeWhichWillBeSet;
        this.typeWhichWillBeReplaced = getLevelBuilder().getCurrentTypeOfField(coordinate, typeWhichWillBeSet);
    }

    @Override
    public void execute() {
        check(coordinate, typeWhichWillBeReplaced);
    }

    private void check(Coordinate coordinate, Type typeWhichWillBeReplaced) {
        if (getLevelBuilder().isInsideOfBoard(coordinate) && typeWhichWillBeReplaced == this.typeWhichWillBeReplaced) {
            SetTask setTask = new SetTask(getLevelBuilder(), coordinate, typeWhichWillBeSet);
            setTask.execute();
            tasksExecuted.add(setTask);
            check(coordinate.getNextCoordinate(Direction.NORTH), typeWhichWillBeReplaced);
            check(coordinate.getNextCoordinate(Direction.SOUTH), typeWhichWillBeReplaced);
            check(coordinate.getNextCoordinate(Direction.EAST), typeWhichWillBeReplaced);
            check(coordinate.getNextCoordinate(Direction.WEST), typeWhichWillBeReplaced);
        }
    }

    @Override
    public void undo() {
        for (Task task : tasksExecuted) {
            task.undo();
        }
    }
}
