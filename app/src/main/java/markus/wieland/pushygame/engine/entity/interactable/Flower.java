package markus.wieland.pushygame.engine.entity.interactable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.InteractableEntity;
import markus.wieland.pushygame.engine.events.FlowerEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Flower extends InteractableEntity {

    public Flower(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        game.execute(new FlowerEvent(this, direction));
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return true;
    }

}
