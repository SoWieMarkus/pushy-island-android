package markus.wieland.pushygame.engine.level;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class RawLevel {
    private String name;
    private String version;
    private long number;
    private List<List<TerrainType>> terrain;
    private List<List<EntityType>> entities;

    public String getVersion() {
        return version;
    }

    public RawLevel() {
    }

    public RawLevel(String hex) {
        terrain = new ArrayList<>(new ArrayList<>());
        entities = new ArrayList<>(new ArrayList<>());

        String binary = LevelBuilder.hexToBinary(hex);
        this.version = binary.substring(0, 8);
        binary = binary.substring(8);

        for (int x = 0; x < LevelBuilder.LEVEL_HEIGHT; x++) {
            terrain.add(new ArrayList<>());
            for (int y = 0; y < LevelBuilder.LEVEL_WIDTH; y++) {
                String tagValue = binary.substring(0, 6);
                binary = binary.substring(6);
                TerrainType terrainType = (TerrainType) Type.getByValue(tagValue, true);
                terrain.get(x).add(terrainType);
            }
        }
        for (int x = 0; x < LevelBuilder.LEVEL_HEIGHT; x++) {
            entities.add(new ArrayList<>());
            for (int y = 0; y < LevelBuilder.LEVEL_WIDTH; y++) {
                String tagValue = binary.substring(0, 6);
                binary = binary.substring(6);
                EntityType entityType = (EntityType) Type.getByValue(tagValue, false);
                entities.get(x).add(entityType);
            }
        }
        this.name = LevelBuilder.binaryStringToString(binary);
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
