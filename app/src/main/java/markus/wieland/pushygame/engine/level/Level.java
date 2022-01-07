package markus.wieland.pushygame.engine.level;

import java.util.List;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class Level {

    private final int height;
    private final String name;
    private final long number;
    private final int width;
    private final Matrix<Terrain> terrain;
    private final Matrix<Entity> entities;

    public Level(RawLevel rawLevel) {
        this.height = rawLevel.getHeight();
        this.name = rawLevel.getName();
        this.number = rawLevel.getNumber();
        this.width = rawLevel.getWidth();
        this.terrain = new Matrix<>(rawLevel.getHeight(), rawLevel.getWidth());
        this.entities = new Matrix<>(rawLevel.getHeight(), rawLevel.getWidth());
        init(rawLevel.getTerrain(), rawLevel.getEntities());
    }

    public int getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    public long getNumber() {
        return number;
    }

    public int getWidth() {
        return width;
    }

    public Matrix<Terrain> getTerrain() {
        return terrain;
    }

    public Matrix<Entity> getEntities() {
        return entities;
    }

    private void init(List<List<TerrainType>> terrain, List<List<EntityType>> entities) {
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                Terrain terrainTile = TileMapBuilder.build(terrain.get(x).get(y), new Coordinate(x,y));
                this.terrain.set(x, y, terrainTile);
                Entity entity = TileMapBuilder.build(entities.get(x).get(y), new Coordinate(x,y));
                this.entities.set(x, y, entity);
            }
        }
    }
}
