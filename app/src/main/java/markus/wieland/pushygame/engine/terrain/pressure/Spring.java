package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.events.TeleportEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Spring extends PressurePlateTerrain {

    private Entity previousEntity;

    public Spring(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
        previousEntity = null;
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (entity instanceof Pushy && !(previousEntity instanceof Pushy)) {
            Coordinate to = getCoordinate().getNextCoordinate(((Pushy) entity).getFacing());
            game.execute(new TeleportEvent(getCoordinate(), to));
            previousEntity = entity.isNotTeleported() ? entity : null;
            return;
        }
        previousEntity = entity;
    }
}
