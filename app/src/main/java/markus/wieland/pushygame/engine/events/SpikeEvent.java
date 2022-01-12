package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.terrain.Spikes;

public class SpikeEvent extends Event {

    private final boolean active;

    public SpikeEvent(boolean active) {
        super();
        this.active = active;
    }

    @Override
    public void execute() {
        for (Spikes spikes : game.getTerrainManager().getOfType(Spikes.class)) {
            Entity entity = game.getEntityManager().getObject(spikes);
            spikes.setActive(active, entity);
            game.getTerrainManager().invalidate(spikes);
        }
    }
}
