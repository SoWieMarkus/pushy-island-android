package markus.wieland.pushygame.engine.entity.statics;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class Stone extends Entity {

    public Stone(Coordinate coordinate) {
        super(coordinate, R.drawable.stone);
    }

    @Override
    public boolean isExplodable() {
        return true;
    }

    @Override
    public boolean destroysFlyingStone() {
        return true;
    }
}
