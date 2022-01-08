package markus.wieland.pushygame.engine.entity.collectible;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Pearl extends CollectibleEntity {

    public Pearl(Coordinate coordinate) {
        super(coordinate, R.drawable.pearl);
    }

    @Override
    public EntityType getType() {
        return EntityType.PEARL;
    }
}
