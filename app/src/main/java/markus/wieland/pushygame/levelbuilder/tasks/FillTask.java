package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class FillTask extends MultipleTask {

    private final Coordinate coordinate;
    private final Type typeWhichWillBeSet;
    private final Type typeWhichWillBeReplaced;

    public FillTask(LevelBuilder levelBuilder, Coordinate coordinate, Type typeWhichWillBeSet) {
        super(levelBuilder);
        this.coordinate = coordinate;
        this.typeWhichWillBeSet = typeWhichWillBeSet;
        this.typeWhichWillBeReplaced = getLevelBuilder().getCurrentTypeOfField(coordinate, typeWhichWillBeSet);
    }

    @Override
    public void execute() {
        if (typeWhichWillBeReplaced == typeWhichWillBeSet) return;
        check(coordinate);
    }

    private void check(Coordinate coordinate) {
        if (!getLevelBuilder().isInsideOfBoard(coordinate)) return;

        Type type = getLevelBuilder().getCurrentTypeOfField(coordinate, typeWhichWillBeSet);
        if (type == this.typeWhichWillBeReplaced) {
            executeSubTask(new SetTask(getLevelBuilder(), coordinate, typeWhichWillBeSet));
            check(coordinate.getNextCoordinate(Direction.NORTH));
            check(coordinate.getNextCoordinate(Direction.SOUTH));
            check(coordinate.getNextCoordinate(Direction.EAST));
            check(coordinate.getNextCoordinate(Direction.WEST));
        }
    }
}
