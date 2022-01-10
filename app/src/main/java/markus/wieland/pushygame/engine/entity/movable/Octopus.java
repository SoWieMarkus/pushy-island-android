package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.terrain.WaterHole;

public class Octopus extends MovableEntity {

    public Octopus(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        super.executeMove(nextCoordinate, game);
        if (game.getTerrainManager().getObject(this) instanceof WaterHole) {
            game.getEntityManager().remove(this);
        }
    }


}
