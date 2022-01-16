package markus.wieland.pushygame.levelbuilder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import markus.wieland.pushygame.engine.EntityManager;
import markus.wieland.pushygame.engine.TerrainManager;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.interactable.Finish;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.Level;
import markus.wieland.pushygame.engine.level.RawLevel;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.TileMapBuilder;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.engine.terrain.Cable;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.levelbuilder.tasks.FillTask;
import markus.wieland.pushygame.levelbuilder.tasks.PlaceCombinationTask;
import markus.wieland.pushygame.levelbuilder.tasks.ReplaceTask;
import markus.wieland.pushygame.levelbuilder.tasks.ResetTask;
import markus.wieland.pushygame.levelbuilder.tasks.SetLogicTask;
import markus.wieland.pushygame.levelbuilder.tasks.SetTask;
import markus.wieland.pushygame.levelbuilder.tasks.SmoothTask;
import markus.wieland.pushygame.levelbuilder.tasks.Task;
import markus.wieland.pushygame.ui.game.PushyFieldView;

public class LevelBuilder {

    public static final int LEVEL_HEIGHT = 12;
    public static final int LEVEL_WIDTH = 20;

    private Type selectedField;

    private final Matrix<PushyFieldView<Terrain>> pushyTerrainViews;
    private final Matrix<PushyFieldView<Entity>> pushyEntityViews;

    private final EntityManager entityManager;
    private final TerrainManager terrainManager;

    private boolean isFillMode;

    private final Activity activity;

    public LevelBuilder(Activity activity) {
        isFillMode = false;
        pushyTerrainViews = new Matrix<>(LEVEL_HEIGHT, LEVEL_WIDTH);
        pushyEntityViews = new Matrix<>(LEVEL_HEIGHT, LEVEL_WIDTH);

        this.activity = activity;

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

    public boolean isNotInsideOfBoard(Coordinate coordinate) {
        return coordinate.getX() < 0 || coordinate.getX() >= LEVEL_HEIGHT || coordinate.getY() < 0 || coordinate.getY() >= LEVEL_WIDTH;
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

    public void reset() {
        TaskManager.getInstance().execute(new ResetTask(this));
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


    public boolean validate() {
        if (entityManager.getOfType(Finish.class).isEmpty()) {
            Toast.makeText(activity, "Please place a finish!", Toast.LENGTH_SHORT).show();
            return false;

        }
        if (entityManager.getOfType(Pushy.class).isEmpty()) {
            Toast.makeText(activity, "Please place a pushy!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }

    public boolean isFillMode() {
        return isFillMode;
    }

    public void smooth() {
        TaskManager.getInstance().execute(new SmoothTask(this));
    }

    public void fill() {
        isFillMode = !isFillMode;
    }

    public boolean selectedFieldIsOneOf(Type... type) {
        for (Type part : type) {
            if (selectedField == part) return true;
        }
        return false;
    }

    public void set(Coordinate coordinate) {
        Task task;

        if (selectedFieldIsOneOf(EntityType.PIRATE_HUT, EntityType.PIRATE)) {
            task = new PlaceCombinationTask(this, coordinate, selectedField);
        } else if (selectedField.getAmountOfAllowedInstances() != Type.UNLIMITED) {
            task = new ReplaceTask(this, selectedField, coordinate);
        } else if (selectedFieldIsOneOf(EntityType.COUNT_DOWN, EntityType.LEVER, EntityType.BUTTON, EntityType.LAMP, TerrainType.CABLE)) {
            task = new SetLogicTask(this, coordinate, selectedField);
        } else {
            task = isFillMode ? new FillTask(this, coordinate, selectedField) : new SetTask(this, coordinate, selectedField);
        }

        TaskManager.getInstance().execute(task);
    }


    /**
     * Get the level as a hex string
     * 1. byte ... version number
     * then 12*20x 6 bit == terrainType.getValue();
     * then 12*20x 6 bit == entityType.getValue();
     * <p>
     * = 2 * 6 bit * 12 * 20 + 1 byte = 361 byte
     * + following will be the level name
     *
     * @return
     */

    public String export() {
        // If I want to change something which would make "old" level corrupted I want to increase the version number and
        // make new Parser for the new version

        createThumbnail(activity, terrainManager, entityManager);

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
        binary += stringToBinary("Levelname");
        return binaryToHex(binary);
    }

    public void importLevel(String hex) {
        Level level = new Level(new RawLevel(hex));
        for (int x = 0; x < LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LEVEL_WIDTH; y++) {
                Coordinate coordinate = new Coordinate(x, y);
                entityManager.setObject(coordinate, level.getEntities().get(coordinate));
                terrainManager.setObject(coordinate, level.getTerrain().get(coordinate));
            }
        }

        for (Cable cable : terrainManager.getOfType(Cable.class)) {
            cable.updateDrawables(this);
        }
    }


    public static String hexToBinary(String hex) {
        List<String> bytes = getParts(hex, 1);
        StringBuilder binaryString = new StringBuilder();
        for (String byteValue : bytes) {
            int i = Integer.parseInt(byteValue, 16);
            String byteAsBinaryString = Type.addRedundantZeros(Integer.toBinaryString(i), 4);
            binaryString.append(byteAsBinaryString);
        }
        return binaryString.toString();
    }

    public static String binaryToHex(String binary) {
        List<String> bytes = getParts(binary, 4);
        StringBuilder hexString = new StringBuilder();

        for (String byteValue : bytes) {
            int decimal = Integer.parseInt(byteValue, 2);
            String byteAsHexString = Integer.toString(decimal, 16);
            hexString.append(byteAsHexString);
        }
        return hexString.toString();
    }

    public static List<String> getParts(String string, int partitionSize) {
        List<String> parts = new ArrayList<>();
        int len = string.length();
        for (int i = 0; i < len; i += partitionSize) {
            parts.add(string.substring(i, Math.min(len, i + partitionSize)));
        }
        return parts;
    }

    public static String binaryStringToString(String binary) {

        List<String> bytes = getParts(binary, 8);
        StringBuilder hexString = new StringBuilder();

        for (String byteValue : bytes) {
            int decimal = Integer.parseInt(byteValue, 2);
            char character = (char) decimal;
            hexString.append(character);
        }
        return hexString.toString();
    }

    public static String stringToBinary(String name) {
        char[] chars = name.toCharArray();
        StringBuilder binary = new StringBuilder();
        for (char character : chars) {
            binary.append("0");
            binary.append(Integer.toBinaryString(character));
        }
        return binary.toString();
    }

    public boolean hasChanges() {
        return TaskManager.getInstance().hasChanges();
    }

    private static final int TILE_SIZE = 8;

    public static Bitmap createThumbnail(Activity activity, TerrainManager terrainManager, EntityManager entityManager) {
        Bitmap thumbnailBitmap = Bitmap.createBitmap(TILE_SIZE * LEVEL_WIDTH, TILE_SIZE * LEVEL_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas thumbnail = new Canvas(thumbnailBitmap);

        for (int x = 0; x < LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LEVEL_WIDTH; y++) {
                int height = x * TILE_SIZE;
                int width = y * TILE_SIZE;

                Coordinate coordinate = new Coordinate(x, y);

                for (int drawable : terrainManager.getObject(coordinate).getDrawableList()) {
                    Drawable d = activity.getResources().getDrawable(drawable, null);
                    d.setBounds(width, height, width + TILE_SIZE, height + TILE_SIZE);
                    d.draw(thumbnail);
                }



                Entity entity = entityManager.getObject(coordinate);
                if (entity == null) continue;

                for (int drawable : entity.getDrawableList()) {
                    Drawable d2 = activity.getResources().getDrawable(drawable, null);
                    d2.setBounds(width, height, width + TILE_SIZE, height + TILE_SIZE);
                    d2.draw(thumbnail);
                }


            }
        }
        return thumbnailBitmap;
    }
}

