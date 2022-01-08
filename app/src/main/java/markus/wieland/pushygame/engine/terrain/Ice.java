package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.TileMapBuilder;
import markus.wieland.pushygame.engine.terrain.pressure.PressurePlateTerrain;

public class Ice extends PressurePlateTerrain {

    private boolean wasVisited;

    public Ice(Coordinate coordinate) {
        super(coordinate, R.drawable.ice);
        wasVisited = false;
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (entity != null) {
            wasVisited = true;
            return;
        }
        if (wasVisited) {
            game.getTerrainManager().setObject(getCoordinate(), TileMapBuilder.build(TerrainType.WATER, getCoordinate()));
        }
    }

    @Override
    public TerrainType getType() {
        return TerrainType.ICE;
    }
}
