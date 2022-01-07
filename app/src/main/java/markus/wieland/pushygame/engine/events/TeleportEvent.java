package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class TeleportEvent extends Event{

    private final Coordinate currentCoordinate;
    private final Coordinate nextCoordinate;

    public TeleportEvent(Coordinate currentCoordinate, Coordinate nextCoordinate) {
        super();
        this.currentCoordinate = currentCoordinate;
        this.nextCoordinate = nextCoordinate;
    }
    @Override
    public void execute() {
        Entity to = game.getEntityManager().getObject(nextCoordinate);
        Entity from = game.getEntityManager().getObject(currentCoordinate);
        if (to == null || to instanceof CollectibleEntity){
            from.setTeleported(true);
            game.getEntityManager().swapFields(currentCoordinate, nextCoordinate);
        }

    }
}
