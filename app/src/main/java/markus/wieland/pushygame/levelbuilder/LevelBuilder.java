package markus.wieland.pushygame.levelbuilder;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.EntityManager;
import markus.wieland.pushygame.engine.TerrainManager;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.Tag;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.TileMapBuilder;
import markus.wieland.pushygame.engine.terrain.Grass;
import markus.wieland.pushygame.engine.terrain.Sand;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.engine.terrain.Water;
import markus.wieland.pushygame.levelbuilder.tasks.FillTask;
import markus.wieland.pushygame.levelbuilder.tasks.SetTask;
import markus.wieland.pushygame.ui.PushyFieldView;

public class LevelBuilder {

    public static final int LEVEL_HEIGHT = 12;
    public static final int LEVEL_WIDTH = 20;

    private Tag selectedField;

    private final Matrix<PushyFieldView<Terrain>> pushyTerrainViews;
    private final Matrix<PushyFieldView<Entity>> pushyEntityViews;

    private final EntityManager entityManager;
    private final TerrainManager terrainManager;

    public LevelBuilder(Activity activity) {

        pushyTerrainViews = new Matrix<>(LEVEL_HEIGHT, LEVEL_WIDTH);
        pushyEntityViews = new Matrix<>(LEVEL_HEIGHT, LEVEL_WIDTH);

        for (int x = 0; x < LEVEL_HEIGHT; x++) {
            for (int y = 0; y < LEVEL_WIDTH; y++) {
                Coordinate coordinate = new Coordinate(x, y);
                PushyFieldView<Terrain> pushyTerrainView = new PushyFieldView<>(activity, new Water(coordinate));
                pushyTerrainViews.set(x, y, pushyTerrainView);
                PushyFieldView<Entity> pushyEntityView = new PushyFieldView<>(activity, (Entity) null);
                pushyEntityViews.set(x, y, pushyEntityView);
                pushyEntityView.invalidate();
                pushyTerrainView.invalidate();
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

    public Tag getSelectedField() {
        return selectedField;
    }

    public Tag getCurrentTypeOfField(Coordinate coordinate) {
        return selectedField;
    }

    public Tag getCurrentTypeOfField(Coordinate coordinate, Tag tag) {
        if (tag instanceof TerrainType) {
            return terrainManager.getObject(coordinate).getType();
        }
        return entityManager.getObject(coordinate) == null ? null : entityManager.getObject(coordinate).getType();
    }

    public void setTypeOfField(Coordinate coordinate, Tag tag) {
        if (tag instanceof TerrainType) {
            terrainManager.setObject(coordinate, TileMapBuilder.build((TerrainType) tag, coordinate));
            return;
        }
        entityManager.setObject(coordinate, TileMapBuilder.build((EntityType) tag, coordinate));
    }

    public void setSelectedField(Tag selectedField) {
        this.selectedField = selectedField;
    }

    public void validate() {

    }

    public void fill(Coordinate coordinate) {
        TaskManager.getInstance().execute(new FillTask(this, coordinate, selectedField));
    }

    public void set(Coordinate coordinate) {
        TaskManager.getInstance().execute(new SetTask(this, coordinate, selectedField));
    }


    public static List<Terrain> getTerrainWhichCanBePlaced() {
        List<Terrain> terrains = new ArrayList<>();
        terrains.add(new Water(null));
        terrains.add(new Sand(null, TerrainType.SAND));
        terrains.add(new Sand(null, TerrainType.SAND_BOTTOM_RIGHT));
        terrains.add(new Sand(null, TerrainType.SAND_BOTTOM_LEFT));
        terrains.add(new Sand(null, TerrainType.SAND_TOP_LEFT));
        terrains.add(new Sand(null, TerrainType.SAND_TOP_RIGHT));

        terrains.add(new Grass(null, TerrainType.GRASS));
        terrains.add(new Grass(null, TerrainType.GRASS_BOTTOM_RIGHT));
        terrains.add(new Grass(null, TerrainType.GRASS_BOTTOM_LEFT));
        terrains.add(new Grass(null, TerrainType.GRASS_TOP_LEFT));
        terrains.add(new Grass(null, TerrainType.GRASS_TOP_RIGHT));
        return terrains;
    }

    public static List<Entity> getEntitiesWhichCanBePlaced() {
        List<Entity> entities = new ArrayList<>();
        entities.add(new Pushy(null));
        return entities;
    }


}
