package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.events.StatueEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Statue extends MovableEntity {

    public Statue(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    // Never return true here so Pushy can't be moved! Pushy will be moved in the StatueEvent
    @Override
    public boolean move(Coordinate nextCoordinate, Game game) {
        if (isMovePossible(nextCoordinate, game)) {
            executeMove(nextCoordinate, game);
        }
        return false;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        game.execute(new StatueEvent(getCoordinate(), nextCoordinate));
    }

}
