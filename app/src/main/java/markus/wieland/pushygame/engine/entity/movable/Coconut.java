package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.terrain.Hole;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class Coconut extends MovableEntity {

    public Coconut(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    public boolean isMovePossible(Coordinate nextCoordinate, Game game) {
        boolean isGenerallyPossible = super.isMovePossible(nextCoordinate, game);
        if (game.getTerrainManager().getObject(nextCoordinate) == null) return false;
        if (game.getTerrainManager().getObject(this).isCoconutTunnel()
                && !game.getTerrainManager().getObject(nextCoordinate).coconutCanEnterFromTunnel())
            return false;
        return isGenerallyPossible
                || game.getTerrainManager().getObject(nextCoordinate) instanceof Hole;
    }

    @Override
    public boolean move(Coordinate nextCoordinate, Game game) {
        Direction direction = getCoordinate().getDirection(nextCoordinate);
        boolean wasMoved = false;
        while (isMovePossible(nextCoordinate, game)) {
            wasMoved = true;
            executeMove(nextCoordinate, game);
            Terrain terrain = game.getTerrainManager().getObject(this);
            if (terrain instanceof Hole && !((Hole) terrain).isFilled()) {
                ((Hole) terrain).setFilled();
                game.getTerrainManager().invalidate(terrain);
                game.getEntityManager().remove(this);
                break;
            }
            nextCoordinate = nextCoordinate.getNextCoordinate(direction);
        }
        return wasMoved;
    }

}
