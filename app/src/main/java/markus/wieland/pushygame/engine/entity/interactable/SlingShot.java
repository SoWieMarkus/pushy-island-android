package markus.wieland.pushygame.engine.entity.interactable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.InteractableEntity;
import markus.wieland.pushygame.engine.entity.collectible.Shot;
import markus.wieland.pushygame.engine.events.ShotEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class SlingShot extends InteractableEntity {

    public SlingShot(Coordinate coordinate) {
        super(coordinate, R.drawable.slingshot);
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        if (game.getInventory().get(Shot.class, 1)) {
            game.execute(new ShotEvent(this));
        }
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return direction == Direction.SOUTH;
    }

    @Override
    public EntityType getType() {
        return EntityType.SLING_SHOT;
    }
}
