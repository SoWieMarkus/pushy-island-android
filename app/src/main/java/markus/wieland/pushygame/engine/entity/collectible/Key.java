package markus.wieland.pushygame.engine.entity.collectible;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class Key extends CollectibleEntity {
    public Key(Coordinate coordinate) {
        super(coordinate, R.drawable.key);
    }
}
