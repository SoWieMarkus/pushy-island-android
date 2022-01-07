package markus.wieland.pushygame.engine.entity;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;

public abstract class CollectibleEntity extends Entity {

    public CollectibleEntity(Coordinate coordinate, int drawable) {
        super(coordinate, drawable);
    }

    public void collect(Game game) {
        game.getInventory().add(this);
        game.getEntityManager().remove(this);
    }
}
