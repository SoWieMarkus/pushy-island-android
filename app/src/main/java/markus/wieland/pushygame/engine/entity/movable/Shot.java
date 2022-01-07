package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class Shot extends CollectibleEntity {
    public Shot(Coordinate coordinate) {
        super(coordinate, R.drawable.shot);
    }
}
