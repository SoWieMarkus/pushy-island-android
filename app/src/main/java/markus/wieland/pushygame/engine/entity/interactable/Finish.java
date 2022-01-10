package markus.wieland.pushygame.engine.entity.interactable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.InteractableEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Finish extends InteractableEntity {

    public Finish(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        game.finishGame();
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return direction == Direction.SOUTH;
    }

}
