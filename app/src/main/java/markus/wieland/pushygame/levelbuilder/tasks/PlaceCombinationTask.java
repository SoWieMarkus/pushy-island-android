package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class PlaceCombinationTask extends MultipleTask {

    private Coordinate coordinateHut;
    private Coordinate coordinatePirate;

    public PlaceCombinationTask(LevelBuilder levelBuilder, Coordinate coordinate, Type type) {
        super(levelBuilder);

        if (type == EntityType.PIRATE) {
            coordinateHut = coordinate.getNextCoordinate(Direction.SOUTH);
            coordinatePirate = coordinate;
            if (!getLevelBuilder().isInsideOfBoard(coordinateHut)) {
                coordinateHut = coordinate;
                coordinatePirate = coordinate.getNextCoordinate(Direction.NORTH);
            }
            return;
        }
        if (type == EntityType.PIRATE_HUT) {
            coordinatePirate = coordinate.getNextCoordinate(Direction.NORTH);
            coordinateHut = coordinate;
            if (!getLevelBuilder().isInsideOfBoard(coordinatePirate)) {
                coordinatePirate = coordinate;
                coordinateHut = coordinate.getNextCoordinate(Direction.SOUTH);
            }
            return;
        }
        throw new IllegalArgumentException("Illegal type. " + type);
    }

    @Override
    public void execute() {
        executeSubTask(new ReplaceTask(getLevelBuilder(), EntityType.PIRATE, coordinatePirate));
        executeSubTask(new ReplaceTask(getLevelBuilder(), EntityType.PIRATE_HUT, coordinateHut));
    }
}
