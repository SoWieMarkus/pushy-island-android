package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.terrain.InvisibleWater;
import markus.wieland.pushygame.engine.terrain.pressure.InvisibleWaterPressurePlate;

public class ShowInvisibleWaterBlocksEvent extends Event {

    private static boolean executedThisRound = false;

    public static void setExecutedThisRound(boolean executed) {
        executedThisRound = executed;
    }

    @Override
    public void execute() {

        if (executedThisRound) return;
        setExecutedThisRound(true);

        boolean willBeVisible = false;

        for (InvisibleWaterPressurePlate invisibleWaterPressurePlate : game.getTerrainManager().getSubListOfPressurePlates(InvisibleWaterPressurePlate.class)) {
            Entity entity = game.getEntityManager().getObject(invisibleWaterPressurePlate);
            willBeVisible = willBeVisible || entity == null;
        }

        for (InvisibleWater invisibleWater : game.getTerrainManager().getOfType(InvisibleWater.class)) {
            invisibleWater.setVisible(willBeVisible);
            game.getTerrainManager().invalidate(invisibleWater);
        }
    }
}
