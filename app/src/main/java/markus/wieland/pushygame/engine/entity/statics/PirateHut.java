package markus.wieland.pushygame.engine.entity.statics;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class PirateHut extends Entity {

    public PirateHut(Coordinate coordinate) {
        super(coordinate, R.drawable.pirat_hut);
    }

    @Override
    public EntityType getType() {
        return EntityType.PIRATE_HUT;
    }
}
