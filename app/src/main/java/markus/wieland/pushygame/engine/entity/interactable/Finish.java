package markus.wieland.pushygame.engine.entity.interactable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.InteractableEntity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Finish extends InteractableEntity {

    public Finish(Coordinate coordinate) {
        super(coordinate, R.drawable.finish);
    }


    @Override
    protected void executeInteraction(Direction direction, Game game) {
        game.finishGame();
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return direction == Direction.SOUTH;
    }

    @Override
    public EntityType getType() {
        return EntityType.FINISH;
    }
}
