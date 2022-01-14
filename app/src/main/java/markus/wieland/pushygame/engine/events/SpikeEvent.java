package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.terrain.Spikes;
import markus.wieland.pushygame.engine.terrain.pressure.SpikePressurePlate;

public class SpikeEvent extends Event {

    private static boolean executedThisRound = false;

    public static void setExecutedThisRound(boolean executedThisRound) {
        SpikeEvent.executedThisRound = executedThisRound;
    }

    @Override
    public void execute() {

        if (executedThisRound) return;
        setExecutedThisRound(true);

        boolean willBeActive = false;

        for (SpikePressurePlate spikePressurePlate : game.getTerrainManager().getSubListOfPressurePlates(SpikePressurePlate.class)) {
            Entity entity = game.getEntityManager().getObject(spikePressurePlate);
            willBeActive = willBeActive || entity == null;
        }

        for (Spikes spikes : game.getTerrainManager().getOfType(Spikes.class)) {
            Entity entity = game.getEntityManager().getObject(spikes);
            spikes.setActive(willBeActive, entity);
            game.getTerrainManager().invalidate(spikes);
        }
    }
}
