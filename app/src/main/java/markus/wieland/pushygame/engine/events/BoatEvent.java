package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class BoatEvent extends Event{

    private final Coordinate currentCoordinate;
    private Coordinate nextCoordinate;

    public BoatEvent(Coordinate currentCoordinate, Coordinate nextCoordinate) {
        super();
        this.currentCoordinate = new Coordinate(currentCoordinate.getX(), currentCoordinate.getY());
        this.nextCoordinate = new Coordinate(nextCoordinate.getX(), nextCoordinate.getY());
    }

    @Override
    public void execute() {
        Direction direction = currentCoordinate.getDirection(nextCoordinate);
        Entity entity = game.getEntityManager().getObject(nextCoordinate);
        Terrain terrain = game.getTerrainManager().getObject(nextCoordinate);


        while (!game.getEntityManager().isNotInsideField(nextCoordinate) && entity == null && terrain.getElevation() == Terrain.ELEVATION_WATER) {
            nextCoordinate = nextCoordinate.getNextCoordinate(direction);
            entity = game.getEntityManager().getObject(nextCoordinate);
            terrain = game.getTerrainManager().getObject(nextCoordinate);
        }

        game.getEntityManager().swapFields(currentCoordinate, nextCoordinate.getNextCoordinate(direction.getOppositeDirection()));
        game.getTerrainManager().swapFields(currentCoordinate, nextCoordinate.getNextCoordinate(direction.getOppositeDirection()));
    }
}
