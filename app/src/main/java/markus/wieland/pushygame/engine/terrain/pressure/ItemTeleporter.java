package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class ItemTeleporter extends Teleporter {
    public ItemTeleporter(Coordinate coordinate) {
        super(coordinate, R.drawable.item_teleporter);
    }

    public boolean isTeleportable(Entity entity) {
        if (entity == null) return false;
        return !entity.isTeleported();
    }

    @Override
    public TerrainType getType() {
        return TerrainType.ITEM_TELEPORTER;
    }
}
