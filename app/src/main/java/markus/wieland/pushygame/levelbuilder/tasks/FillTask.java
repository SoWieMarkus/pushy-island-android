package markus.wieland.pushygame.levelbuilder.tasks;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.Tag;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class FillTask extends Task {

    private final Coordinate coordinate;
    private final List<Task> tasksExecuted;
    private final Tag tagWhichWillBeSet;
    private final Tag tagWhichWillBeReplaced;

    public FillTask(LevelBuilder levelBuilder, Coordinate coordinate, Tag tagWhichWillBeSet) {
        super(levelBuilder);
        this.coordinate = coordinate;
        this.tasksExecuted = new ArrayList<>();
        this.tagWhichWillBeSet = tagWhichWillBeSet;
        this.tagWhichWillBeReplaced = getLevelBuilder().getCurrentTypeOfField(coordinate, tagWhichWillBeSet);
    }

    @Override
    public void execute() {
        check(coordinate, tagWhichWillBeReplaced);
    }

    private void check(Coordinate coordinate, Tag tagWhichWillBeReplaced) {
        if (getLevelBuilder().isInsideOfBoard(coordinate) && tagWhichWillBeReplaced == this.tagWhichWillBeReplaced) {
            SetTask setTask = new SetTask(getLevelBuilder(), coordinate, tagWhichWillBeSet);
            setTask.execute();
            tasksExecuted.add(setTask);
            check(coordinate.getNextCoordinate(Direction.NORTH), tagWhichWillBeReplaced);
            check(coordinate.getNextCoordinate(Direction.SOUTH), tagWhichWillBeReplaced);
            check(coordinate.getNextCoordinate(Direction.EAST), tagWhichWillBeReplaced);
            check(coordinate.getNextCoordinate(Direction.WEST), tagWhichWillBeReplaced);
        }
    }

    @Override
    public void undo() {
        for (Task task : tasksExecuted) {
            task.undo();
        }
    }
}
