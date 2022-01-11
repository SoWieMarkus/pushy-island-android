package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.terrain.Sand;
import markus.wieland.pushygame.engine.terrain.Terrain;

public abstract class PressurePlateTerrain extends Sand {

    public PressurePlateTerrain(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    public abstract void interact(Entity entity, Game game);
}
