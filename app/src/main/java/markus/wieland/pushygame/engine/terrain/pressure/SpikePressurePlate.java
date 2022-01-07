package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.events.SpikeEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class SpikePressurePlate extends PressurePlateTerrain {

    private Entity lastEntity;

    public SpikePressurePlate(Coordinate coordinate) {
        super(coordinate, R.drawable.spike_pressure_plate);
        lastEntity = null;
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (lastEntity == entity) return;
        lastEntity = entity;
        game.execute(new SpikeEvent(entity == null));
    }
}
