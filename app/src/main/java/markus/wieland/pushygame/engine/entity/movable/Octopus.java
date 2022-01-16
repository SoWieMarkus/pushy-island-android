package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.GameFinishEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.terrain.WaterHole;

public class Octopus extends MovableEntity implements GameFinishEntity {

    public Octopus(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    public boolean isMovableEntity() {
        return false;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        super.executeMove(nextCoordinate, game);
        if (game.getTerrainManager().getObject(this) instanceof WaterHole) {
            game.getEntityManager().remove(this);
        }
    }

    @Override
    public boolean check(Game game) {
        return false;
    }
}
