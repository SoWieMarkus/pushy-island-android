package markus.wieland.pushygame.engine.level;

import java.util.List;

public class RawLevel {
    private int height;
    private String name;
    private long number;
    private int width;
    private List<List<TerrainType>> terrain;
    private List<List<EntityType>> entities;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public List<List<TerrainType>> getTerrain() {
        return terrain;
    }

    public void setTerrain(List<List<TerrainType>> terrain) {
        this.terrain = terrain;
    }

    public List<List<EntityType>> getEntities() {
        return entities;
    }

    public void setEntities(List<List<EntityType>> entities) {
        this.entities = entities;
    }
}
