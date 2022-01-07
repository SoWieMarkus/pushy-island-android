package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.events.BombEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class BombField extends PressurePlateTerrain {
    public BombField(Coordinate coordinate) {
        super(coordinate, R.drawable.bomb_field);
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (entity instanceof Pushy) {
            game.execute(new BombEvent());
        }
    }
}
