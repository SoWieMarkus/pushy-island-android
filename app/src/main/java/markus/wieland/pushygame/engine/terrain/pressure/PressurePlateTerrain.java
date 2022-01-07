package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.events.Event;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.terrain.Terrain;

public abstract class PressurePlateTerrain extends Terrain {

    public PressurePlateTerrain(Coordinate coordinate, int drawable) {
        super(coordinate, drawable);
    }

    @Override
    public int getElevation() {
        return 0;
    }

    public abstract void interact(Entity entity, Game game) ;
}
