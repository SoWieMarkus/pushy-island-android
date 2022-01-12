package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.GameFinishEntity;
import markus.wieland.pushygame.engine.events.StatueEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.terrain.StatueFinish;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class Statue extends MovableEntity implements GameFinishEntity {

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

    @Override
    public boolean check(Game game) {
        Terrain terrain = game.getTerrainManager().getObject(this);
        return terrain instanceof StatueFinish && ((StatueFinish) terrain).equalsType(getType());
    }
}
