package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.events.ShowInvisibleWaterBlocksEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class InvisibleWaterPressurePlate extends PressurePlateTerrain {

    private Entity lastEntity;

    public InvisibleWaterPressurePlate(Coordinate coordinate) {
        super(coordinate, R.drawable.water_invisible_pressure_plate);
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (entity == lastEntity) return;
        lastEntity = entity;
        game.execute(new ShowInvisibleWaterBlocksEvent(entity != null));
    }
}
