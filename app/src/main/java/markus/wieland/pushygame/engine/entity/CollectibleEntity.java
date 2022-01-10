package markus.wieland.pushygame.engine.entity;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public abstract class CollectibleEntity extends Entity {

    public CollectibleEntity(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    public void collect(Game game) {
        game.getInventory().add(this);
        game.getEntityManager().remove(this);
    }
}
