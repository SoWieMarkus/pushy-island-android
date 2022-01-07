package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class ItemTeleporter extends Teleporter {
    public ItemTeleporter(Coordinate coordinate) {
        super(coordinate, R.drawable.item_teleporter);
    }

    public boolean isTeleportable(Entity entity) {
        if (entity == null) return false;
        return !entity.isTeleported();
    }
}
