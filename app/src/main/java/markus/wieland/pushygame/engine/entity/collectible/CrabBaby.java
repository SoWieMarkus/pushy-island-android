package markus.wieland.pushygame.engine.entity.collectible;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class CrabBaby extends CollectibleEntity {

    public CrabBaby(Coordinate coordinate) {
        super(coordinate, R.drawable.crab_baby);
    }

    @Override
    public EntityType getType() {
        return EntityType.CRAB;
    }
}
