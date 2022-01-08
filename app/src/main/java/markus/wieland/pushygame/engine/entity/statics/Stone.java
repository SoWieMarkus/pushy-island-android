package markus.wieland.pushygame.engine.entity.statics;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Stone extends Entity {

    public Stone(Coordinate coordinate) {
        super(coordinate, R.drawable.stone);
    }

    @Override
    public boolean isExplodable() {
        return true;
    }

    @Override
    public EntityType getType() {
        return EntityType.STONE;
    }

    @Override
    public boolean destroysFlyingStone() {
        return true;
    }
}
