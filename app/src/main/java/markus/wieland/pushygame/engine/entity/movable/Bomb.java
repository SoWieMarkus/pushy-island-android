package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Bomb extends MovableEntity {

    public Bomb(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    public void explode(Game game) {
        explodeEntity(game, getCoordinate().getNextCoordinate(Direction.NORTH));
        explodeEntity(game, getCoordinate().getNextCoordinate(Direction.SOUTH));
        explodeEntity(game, getCoordinate().getNextCoordinate(Direction.EAST));
        explodeEntity(game, getCoordinate().getNextCoordinate(Direction.WEST));
        game.getEntityManager().remove(this);
    }

    private void explodeEntity(Game game, Coordinate coordinate) {
        if (game.getEntityManager().isNotInsideField(coordinate)) return;
        Entity entity = game.getEntityManager().getObject(coordinate);
        if (entity != null && entity.isExplodable()) game.getEntityManager().remove(entity);
    }

}
