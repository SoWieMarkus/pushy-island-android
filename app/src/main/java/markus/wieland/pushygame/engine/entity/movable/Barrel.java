package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.GameFinishEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.terrain.BarrelFinish;

public class Barrel extends MovableEntity implements GameFinishEntity {

    public Barrel(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    public boolean check(Game game) {
        return game.getTerrainManager().getObject(this) instanceof BarrelFinish;
    }
}
