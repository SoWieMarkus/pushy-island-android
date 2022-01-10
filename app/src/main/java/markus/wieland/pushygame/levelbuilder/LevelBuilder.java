package markus.wieland.pushygame.levelbuilder;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.EntityManager;
import markus.wieland.pushygame.engine.TerrainManager;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.TileMapBuilder;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.levelbuilder.tasks.FillTask;
import markus.wieland.pushygame.levelbuilder.tasks.SetTask;
import markus.wieland.pushygame.levelbuilder.tasks.SmoothTask;
import markus.wieland.pushygame.ui.PushyFieldView;

public class LevelBuilder {

    public static final int LEVEL_HEIGHT = 12;
    public static final int LEVEL_WIDTH = 20;

    private Type selectedField;

    private final Matrix<PushyFieldView<Terrain>> pushyTerrainViews;
    private final Matrix<PushyFieldView<Entity>> pushyEntityViews;

    private final EntityManager entityManager;
    private final TerrainManager terrainManager;

    private boolean isFillMode;

    public LevelBuilder(Activity activity) {
        isFillMode = false;
        pushyTerrainViews = new Matrix<>(LEVEL_HEIGHT, LEVEL_WIDTH);
        pushyEntityViews = new Matrix<>(LEVEL_HEIGHT, LEVEL_WIDTH);

        for (int x = 0; x < LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LEVEL_WIDTH; y++) {
                Coordinate coordinate = new Coordinate(x, y);
                PushyFieldView<Terrain> pushyTerrainView = new PushyFieldView<>(activity, TileMapBuilder.build(TerrainType.WATER, coordinate));
                pushyTerrainViews.set(x, y, pushyTerrainView);
                PushyFieldView<Entity> pushyEntityView = new PushyFieldView<>(activity, (Entity) null);
                pushyEntityViews.set(x, y, pushyEntityView);
                pushyEntityView.invalidate();
                pushyTerrainView.invalidate();
                pushyEntityView.setOnClickListener(view -> set(coordinate));
            }
        }
        this.entityManager = new EntityManager(pushyEntityViews);
        this.terrainManager = new TerrainManager(pushyTerrainViews);
        this.selectedField = TerrainType.SAND;
    }

    public Matrix<PushyFieldView<Terrain>> getPushyTerrainViews() {
        return pushyTerrainViews;
    }

    public Matrix<PushyFieldView<Entity>> getPushyEntityViews() {
        return pushyEntityViews;
    }

    public boolean isInsideOfBoard(Coordinate coordinate) {
        return coordinate.getX() >= 0 && coordinate.getX() < LEVEL_HEIGHT && coordinate.getY() >= 0 && coordinate.getY() < LEVEL_WIDTH;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public TerrainManager getTerrainManager() {
        return terrainManager;
    }

    public Type getSelectedField() {
        return selectedField;
    }

    public Type getCurrentTypeOfField(Coordinate coordinate) {
        return selectedField;
    }

    public void undo() {
        TaskManager.getInstance().undo();
    }

    public void redo() {
        TaskManager.getInstance().redo();
    }

    public Type getCurrentTypeOfField(Coordinate coordinate, Type type) {
        if (type instanceof TerrainType) {
            return terrainManager.getObject(coordinate).getType();
        }
        return entityManager.getObject(coordinate) == null ? null : entityManager.getObject(coordinate).getType();
    }

    public void setTypeOfField(Coordinate coordinate, Type type) {
        if (type instanceof TerrainType) {
            terrainManager.setObject(coordinate, TileMapBuilder.build((TerrainType) type, coordinate));
            return;
        }
        entityManager.setObject(coordinate, TileMapBuilder.build((EntityType) type, coordinate));
    }

    public void setSelectedField(Type selectedField) {
        this.selectedField = selectedField;
    }


    public void validate() {

    }


    public void smooth() {
        TaskManager.getInstance().execute(new SmoothTask(this));
    }

    public void fill() {
        isFillMode = !isFillMode;

    }

    public void set(Coordinate coordinate) {
        TaskManager.getInstance().execute(isFillMode ? new FillTask(this, coordinate, selectedField) : new SetTask(this, coordinate, selectedField));
    }


    public String export() {
        // If I want to change something which would make "old" level corrupted I want to increase the version number and
        // make new Parser for the new version
        int version = 1;
        String binary = Type.addRedundantZeros(Integer.toBinaryString(version), 8);
        StringBuilder binaryTerrain = new StringBuilder();
        StringBuilder binaryEntity = new StringBuilder();
        for (int x = 0; x < LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LEVEL_WIDTH; y++) {
                Coordinate coordinate = new Coordinate(x, y);
                binaryTerrain.append(terrainManager.getObject(coordinate).getType().getValue());
                Entity entity = entityManager.getObject(coordinate);
                binaryEntity.append(entity == null ? EntityType.NO_ENTITY.getValue() : entity.getType().getValue());
            }
        }

        binary += binaryTerrain.toString() + binaryEntity.toString();
        return binaryToHex(binary);
    }

    public void importLevel(String hex) {
        String binary = hexToBinary(hex);
        String version = binary.substring(0, 8);
        binary = binary.substring(8);

        for (int x = 0; x < LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LEVEL_WIDTH; y++) {
                String tagValue = binary.substring(0, 6);
                binary = binary.substring(6);

                TerrainType terrainType = (TerrainType) Type.getByValue(tagValue, true);
                Coordinate coordinate = new Coordinate(x, y);
                terrainManager.setObject(coordinate, TileMapBuilder.build(terrainType, coordinate));
            }
        }
        for (int x = 0; x < LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LEVEL_WIDTH; y++) {
                String tagValue = binary.substring(0, 6);
                binary = binary.substring(6);
                EntityType entityType = (EntityType) Type.getByValue(tagValue, false);
                Coordinate coordinate = new Coordinate(x, y);
                entityManager.setObject(coordinate, TileMapBuilder.build(entityType, coordinate));
            }
        }

        int x = 0;
    }


    private String hexToBinary(String hex) {
        List<String> bytes = getParts(hex, 1);
        StringBuilder binaryString = new StringBuilder();
        for (String byteValue : bytes) {
            int i = Integer.parseInt(byteValue, 16);
            String byteAsBinaryString = Type.addRedundantZeros(Integer.toBinaryString(i), 4);
            binaryString.append(byteAsBinaryString);
        }
        return binaryString.toString();
    }

    private String binaryToHex(String binary) {
        List<String> bytes = getParts(binary, 4);
        StringBuilder hexString = new StringBuilder();

        for (String byteValue : bytes) {
            int decimal = Integer.parseInt(byteValue, 2);
            String byteAsHexString = Integer.toString(decimal, 16);
            hexString.append(byteAsHexString);
        }
        return hexString.toString();
    }

    private static List<String> getParts(String string, int partitionSize) {
        List<String> parts = new ArrayList<>();
        int len = string.length();
        for (int i = 0; i < len; i += partitionSize) {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }


}

