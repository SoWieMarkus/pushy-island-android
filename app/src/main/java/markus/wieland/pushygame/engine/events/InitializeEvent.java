package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.EntityManager;
import markus.wieland.pushygame.engine.TerrainManager;
import markus.wieland.pushygame.engine.entity.logic.NotGate;
import markus.wieland.pushygame.engine.entity.logic.PowerBlock;
import markus.wieland.pushygame.engine.entity.movable.Box;
import markus.wieland.pushygame.engine.entity.movable.Count;
import markus.wieland.pushygame.engine.entity.movable.SeaStar;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.TileMapBuilder;
import markus.wieland.pushygame.engine.terrain.InvisibleWater;
import markus.wieland.pushygame.engine.terrain.Water;

public class InitializeEvent extends Event{
    @Override
    public void execute() {
        EntityManager entityManager = game.getEntityManager();
        TerrainManager terrainManager = game.getTerrainManager();


        for (NotGate notGate : entityManager.getOfType(NotGate.class)) {
            notGate.update(game);
        }

        for (Box box : entityManager.getOfType(Box.class)) {
            if (terrainManager.getObject(box) instanceof Water) {
                entityManager.remove(box);
                terrainManager.setObject(box.getCoordinate(), TileMapBuilder.build(TerrainType.BOX_WATER, box.getCoordinate()));
            }
        }
        for (SeaStar seaStar : entityManager.getOfType(SeaStar.class)) {
            if (terrainManager.getObject(seaStar) instanceof Water) {
                entityManager.remove(seaStar);
            }
        }
        for (InvisibleWater invisibleWater : terrainManager.getOfType(InvisibleWater.class)) {
            invisibleWater.setVisible(true);
            terrainManager.invalidate(invisibleWater);
        }
        for (Count count : entityManager.getOfType(Count.class)) {
            count.setUncovered(false);
            entityManager.invalidate(count);
        }

        for (PowerBlock powerBlock : entityManager.getOfType(PowerBlock.class)) {
            powerBlock.executePowerEvent(game, powerBlock.getCoordinate(), null);
        }

        game.execute(new UpdateCableEvent());

        StringEvent.setIsStringActive(false);
        SpikeEvent.setExecutedThisRound(false);
        ShowInvisibleWaterBlocksEvent.setExecutedThisRound(false);
        CountEvent.setCurrentCount(1);
    }
}
