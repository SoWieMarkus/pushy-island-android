package markus.wieland.pushygame.engine.level;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.EntityManager;
import markus.wieland.pushygame.engine.TerrainManager;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public class LevelConverter {

    private LevelConverter(){}

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
            binary.append(Type.addRedundantZeros(Integer.toBinaryString(character), 8));
        }
        return binary.toString();
    }

    /**
     * Get the level as a hex string
     * 1. byte ... version number
     * then 12*20x 6 bit == terrainType.getValue();
     * then 12*20x 6 bit == entityType.getValue();
     * <p>
     * = 2 * 6 bit * 12 * 20 + 1 byte = 361 byte
     * + following will be the level name
     */
    // If I want to change something which would make "old" level corrupted I want to increase the version number and
    // make new Parser for the new version
    public static String convertToLevelCode(String levelName, TerrainManager terrainManager, EntityManager entityManager) {
        return convertToLevelCode(levelName, terrainManager.getAllAsMatrix(), entityManager.getAllAsMatrix());
    }

    public static String convertToLevelCode(String levelName, Level level) {
        return convertToLevelCode(levelName, level.getTerrain(), level.getEntities());
    }

    public static String convertToLevelCode(String levelName, Matrix<Terrain> terrain, Matrix<Entity> entities) {
        int version = 1;
        String binary = Type.addRedundantZeros(Integer.toBinaryString(version), 8);
        StringBuilder binaryTerrain = new StringBuilder();
        StringBuilder binaryEntity = new StringBuilder();
        for (int x = 0; x < LevelBuilder.LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LevelBuilder.LEVEL_WIDTH; y++) {
                Coordinate coordinate = new Coordinate(x, y);
                binaryTerrain.append(terrain.get(coordinate).getType().getValue());
                Entity entity = entities.get(coordinate);
                binaryEntity.append(entity == null ? EntityType.NO_ENTITY.getValue() : entity.getType().getValue());
            }
        }
        binary += binaryTerrain.toString() + binaryEntity.toString();
        binary += LevelConverter.stringToBinary(levelName);
        return LevelConverter.binaryToHex(binary);
    }

    private static final int TILE_SIZE = 8;

    public static Bitmap createThumbnail(Activity activity, TerrainManager terrainManager, EntityManager entityManager) {
        Bitmap thumbnailBitmap = Bitmap.createBitmap(TILE_SIZE * LevelBuilder.LEVEL_WIDTH, TILE_SIZE * LevelBuilder.LEVEL_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas thumbnail = new Canvas(thumbnailBitmap);

        for (int x = 0; x < LevelBuilder.LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LevelBuilder.LEVEL_WIDTH; y++) {
                int height = x * TILE_SIZE;
                int width = y * TILE_SIZE;

                Coordinate coordinate = new Coordinate(x, y);

                for (int drawable : terrainManager.getObject(coordinate).getDrawableList()) {
                    drawToCanvas(thumbnail, height, width, drawable, activity);
                }
                Entity entity = entityManager.getObject(coordinate);
                if (entity == null) continue;

                for (int drawable : entity.getDrawableList()) {
                    drawToCanvas(thumbnail, height, width, drawable, activity);
                }
            }
        }
        return thumbnailBitmap;
    }

    private static void drawToCanvas(Canvas canvas, int height, int width, int drawable, Activity activity) {
        Drawable d = activity.getResources().getDrawable(drawable, null);
        d.setBounds(width, height, width + TILE_SIZE, height + TILE_SIZE);
        d.draw(canvas);
    }

    public static Level getLevelFromCode(String code) {
        RawLevel rawLevel = new RawLevel(code);
        return new Level(rawLevel);
    }

}
