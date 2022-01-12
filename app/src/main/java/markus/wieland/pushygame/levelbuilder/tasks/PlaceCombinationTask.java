package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

/**
 * The pirate and his hut is the only combination of fields! They have to be next to each other. So
 * when the user places a pirate it should automatically place a hut underneath (south) him. If the user
 * places a hut, the pirate should be placed on top (north)
 */
public class PlaceCombinationTask extends MultipleTask {

    private Coordinate coordinateHut;
    private Coordinate coordinatePirate;

    public PlaceCombinationTask(LevelBuilder levelBuilder, Coordinate coordinate, Type type) {
        super(levelBuilder);

        if (type == EntityType.PIRATE) {
            coordinateHut = coordinate.getNextCoordinate(Direction.SOUTH);
            coordinatePirate = coordinate;

            // if the hut would be outside of the board, the placement should automatically move one field direction north
            if (!getLevelBuilder().isInsideOfBoard(coordinateHut)) {
                coordinateHut = coordinate;
                coordinatePirate = coordinate.getNextCoordinate(Direction.NORTH);
            }
            return;
        }
        if (type == EntityType.PIRATE_HUT) {
            coordinatePirate = coordinate.getNextCoordinate(Direction.NORTH);
            coordinateHut = coordinate;
            // if the pirate would be outside of the board, the placement should automatically move one field direction south

            if (!getLevelBuilder().isInsideOfBoard(coordinatePirate)) {
                coordinatePirate = coordinate;
                coordinateHut = coordinate.getNextCoordinate(Direction.SOUTH);
            }
            return;
        }
        // At the moment only pirate and hut can be placed at the same time.
        throw new IllegalArgumentException("Illegal type. " + type);
    }

    @Override
    public void execute() {
        executeSubTask(new ReplaceTask(getLevelBuilder(), EntityType.PIRATE, coordinatePirate));
        executeSubTask(new ReplaceTask(getLevelBuilder(), EntityType.PIRATE_HUT, coordinateHut));
    }
}
