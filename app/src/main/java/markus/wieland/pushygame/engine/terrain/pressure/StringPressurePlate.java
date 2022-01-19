package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.events.StringEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class StringPressurePlate extends PressurePlateTerrain {

    private boolean interacted;

    public StringPressurePlate(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
        interacted = false;
    }

    @Override
    public int getDrawable() {
        return interacted ? R.drawable.hole_filled : super.getDrawable();
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (entity instanceof Pushy && !interacted) {
            interacted = true;
            game.execute(new StringEvent());
            game.getTerrainManager().invalidate(this);
        }
    }

}
