package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class ItemTeleporter extends Teleporter {

    public ItemTeleporter(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    @Override
    public boolean isTeleportable(Entity entity) {
        if (entity == null) return false;
        return entity.isNotTeleported();
    }

}
