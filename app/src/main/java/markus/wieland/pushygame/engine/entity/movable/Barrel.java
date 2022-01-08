package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Barrel extends MovableEntity {

    public Barrel(Coordinate coordinate) {
        super(coordinate, R.drawable.barrel);
    }

    @Override
    public EntityType getType() {
        return EntityType.BARREL;
    }
}
