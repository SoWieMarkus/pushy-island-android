package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.events.TeleportEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Spring extends PressurePlateTerrain {

    public Spring(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (entity instanceof Pushy) {
            Coordinate to = getCoordinate().getNextCoordinate(((Pushy) entity).getFacing());
            game.execute(new TeleportEvent(getCoordinate(), to));
        }
    }
}
