package markus.wieland.pushygame.engine;

import android.app.Activity;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.GameFinishEntity;
import markus.wieland.pushygame.engine.entity.interactable.Flower;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.events.Event;
import markus.wieland.pushygame.engine.events.GameEventListener;
import markus.wieland.pushygame.engine.events.InitializeEvent;
import markus.wieland.pushygame.engine.events.InventoryEventListener;
import markus.wieland.pushygame.engine.events.ShowInvisibleWaterBlocksEvent;
import markus.wieland.pushygame.engine.events.SpikeEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.helper.Inventory;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.terrain.ChangeableFlower;
import markus.wieland.pushygame.engine.terrain.FlowerFinish;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.engine.terrain.pressure.PressurePlateTerrain;
import markus.wieland.pushygame.engine.terrain.pressure.Teleporter;
import markus.wieland.pushygame.ui.game.PushyFieldView;

public class Game {

    private final EntityManager entityManager;
    private final TerrainManager terrainManager;
    private final Inventory inventory;
    private GameEventListener gameEventListener;
    private final Activity activity;

    public Game(Activity activity, Matrix<PushyFieldView<Entity>> entityViews, Matrix<PushyFieldView<Terrain>> terrainViews) {
        this.entityManager = new EntityManager(entityViews);
        this.terrainManager = new TerrainManager(terrainViews);
        this.inventory = new Inventory();
        this.activity = activity;

        execute(new InitializeEvent());
    }

    public Activity getActivity() {
        return activity;
    }

    public void setInventoryEventListener(InventoryEventListener inventoryEventListener) {
        this.inventory.setInventoryEventListener(inventoryEventListener);
    }

    public void setGameEventListener(GameEventListener gameEventListener) {
        this.gameEventListener = gameEventListener;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public TerrainManager getTerrainManager() {
        return terrainManager;
    }


    public Inventory getInventory() {
        return inventory;
    }

    public void move(Direction direction) {

        Pushy pushy = entityManager.getPushy();

        Coordinate currentCoordinate = pushy.getCoordinate();
        Coordinate nextCoordinate = currentCoordinate.getNextCoordinate(direction);

        pushy.setFacing(direction);
        entityManager.invalidate(pushy);

        pushy.move(nextCoordinate, this);

        for (PressurePlateTerrain pressurePlateTerrain : terrainManager.getPressurePlateTerrains()) {
            pressurePlateTerrain.interact(entityManager.getObject(pressurePlateTerrain), this);
        }

        for (Entity entity : entityManager.getAll()) {
            entity.setTeleported(terrainManager.getObject(entity) instanceof Teleporter);
        }

        SpikeEvent.setExecutedThisRound(false);
        ShowInvisibleWaterBlocksEvent.setExecutedThisRound(false);
    }

    public void execute(Event event) {
        event.setGame(this);
        event.execute();
    }

    public void finishGame() {
        if (checkGameState()) gameEventListener.onFinish();
    }

    public boolean checkGameState() {
        return checkChangeableFlowers() && checkFlowerFinishes() && checkEntities();
    }

    private boolean checkEntities() {
        for (Entity entity : entityManager.getAll()) {
            if (entity instanceof GameFinishEntity && !((GameFinishEntity) entity).check(this))
                return false;
        }
        return true;
    }

    private boolean checkFlowerFinishes() {
        for (FlowerFinish flowerFinish : terrainManager.getOfType(FlowerFinish.class)) {
            Entity entity = entityManager.getObject(flowerFinish);
            if (entity == null) return false;
            if (!(entity instanceof Flower)) return false;
            if (entity.getType() != flowerFinish.getFlowerType()) return false;
        }
        return true;
    }

    private boolean checkChangeableFlowers() {
        TerrainType changeableFlowerTerrainType = null;
        for (ChangeableFlower changeableFlower : terrainManager.getOfType(ChangeableFlower.class)) {
            if (changeableFlowerTerrainType == null)
                changeableFlowerTerrainType = changeableFlower.getType();
            if (changeableFlowerTerrainType != changeableFlower.getType()) return false;
        }
        return true;
    }

}
