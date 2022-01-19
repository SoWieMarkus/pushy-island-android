package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.events.SpikeEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class SpikePressurePlate extends PressurePlateTerrain {

    public SpikePressurePlate(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    @Override
    public int[] getDrawableList() {
        return new int[]{R.drawable.sand, super.getDrawable()};
    }

    @Override
    public void interact(Entity entity, Game game) {
        game.execute(new SpikeEvent());
    }

}
