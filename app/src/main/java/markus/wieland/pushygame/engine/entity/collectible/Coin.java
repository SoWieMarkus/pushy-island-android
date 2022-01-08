package markus.wieland.pushygame.engine.entity.collectible;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Coin extends CollectibleEntity {

    public Coin(Coordinate coordinate) {
        super(coordinate, R.drawable.coin);
    }

    @Override
    public EntityType getType() {
        return EntityType.COIN;
    }
}
