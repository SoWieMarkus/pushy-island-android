package markus.wieland.pushygame.engine.entity.collectible;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class Seed extends CollectibleEntity {

    public Seed(Coordinate coordinate) {
        super(coordinate, R.drawable.seed);
    }
}
