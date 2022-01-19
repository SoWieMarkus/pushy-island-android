package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.events.TeleportEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Teleporter extends PressurePlateTerrain {

    public Teleporter(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (!isTeleportable(entity)) return;
        game.execute(new TeleportEvent(getCoordinate(), game.getTerrainManager().getOtherTeleporter(this).getCoordinate()));
    }

    public boolean isTeleportable(Entity entity) {
        if (entity == null) return false;
        return entity instanceof Pushy && entity.isNotTeleported();
    }
}
