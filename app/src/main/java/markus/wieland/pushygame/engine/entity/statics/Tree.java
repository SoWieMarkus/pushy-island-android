package markus.wieland.pushygame.engine.entity.statics;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Tree extends Entity {

    public Tree(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    public boolean destroysFlyingStone() {
        return true;
    }
}
