package markus.wieland.pushygame.engine.entity.statics;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Tree extends Entity {

    public Tree(Coordinate coordinate) {
        super(coordinate, R.drawable.tree);
    }

    @Override
    public boolean destroysFlyingStone() {
        return true;
    }

    @Override
    public EntityType getType() {
        return EntityType.TREE;
    }
}
