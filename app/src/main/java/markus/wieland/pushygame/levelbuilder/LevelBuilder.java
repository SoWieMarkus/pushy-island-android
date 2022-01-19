package markus.wieland.pushygame.levelbuilder;

import android.app.Activity;
import android.widget.Toast;

import markus.wieland.pushygame.engine.EntityManager;
import markus.wieland.pushygame.engine.TerrainManager;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.interactable.Finish;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.Level;
import markus.wieland.pushygame.engine.level.LevelConverter;
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

    private boolean saved;

    public LevelBuilder(Activity activity) {
        isFillMode = false;
        pushyTerrainViews = new Matrix<>(LEVEL_HEIGHT, LEVEL_WIDTH);
        pushyEntityViews = new Matrix<>(LEVEL_HEIGHT, LEVEL_WIDTH);

        this.activity = activity;
        TaskManager.getInstance().clear();

        this.saved = false;

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

    public boolean isSaved() {
        return saved || hasNoChanges();
    }

    public void set(Coordinate coordinate) {
        Task task;

        if (selectedFieldIsOneOf(EntityType.PIRATE_HUT, EntityType.PIRATE)) {
            task = new PlaceCombinationTask(this, coordinate, selectedField);
        } else if (selectedField.getAmountOfAllowedInstances() != Type.UNLIMITED) {
            task = new ReplaceTask(this, selectedField, coordinate);
        } else if (selectedField.isLogicPart()) {
            task = new SetLogicTask(this, coordinate, selectedField);
        } else {
            task = isFillMode ? new FillTask(this, coordinate, selectedField) : new SetTask(this, coordinate, selectedField);
        }

        TaskManager.getInstance().execute(task);
    }

    public String export(String levelName) {
        saved = true;
        return LevelConverter.convertToLevelCode(levelName, terrainManager, entityManager);
    }

    public void importLevel(String code) {
        Level level = LevelConverter.getLevelFromCode(code);
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

    public boolean hasNoChanges() {
        return !TaskManager.getInstance().hasChanges();
    }


}

