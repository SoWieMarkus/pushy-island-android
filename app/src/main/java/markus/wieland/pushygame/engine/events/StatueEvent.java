package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.entity.movable.Statue;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;

public class StatueEvent extends Event {

    private final Coordinate currentCoordinate;
    private final Coordinate nextCoordinate;

    public StatueEvent(Coordinate currentCoordinate, Coordinate nextCoordinate) {
        super();
        this.currentCoordinate = currentCoordinate;
        this.nextCoordinate = nextCoordinate;
    }

    @Override
    public void execute() {
        Direction direction = currentCoordinate.getDirection(nextCoordinate);
        Pushy pushy = game.getEntityManager().getPushy();
        game.getEntityManager().remove(pushy);

        Statue statue = (Statue) game.getEntityManager().getObject(currentCoordinate);

        for (Statue statueFromList : game.getEntityManager().getOfType(Statue.class)) {
            if (statue.getEntityType() == statueFromList.getEntityType() && statueFromList.isMovePossible(statueFromList.getCoordinate().getNextCoordinate(direction), game)) {
                game.getEntityManager().swapFields(statueFromList.getCoordinate(), statueFromList.getCoordinate().getNextCoordinate(direction));
            }
        }

        game.getEntityManager().setObject(pushy.getCoordinate().getNextCoordinate(direction), pushy);
        pushy.executeAfterMove(pushy.getCoordinate().getNextCoordinate(direction), game);
    }
}
