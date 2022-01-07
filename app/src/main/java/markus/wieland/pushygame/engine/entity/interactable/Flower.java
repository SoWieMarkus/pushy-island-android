package markus.wieland.pushygame.engine.entity.interactable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.InteractableEntity;
import markus.wieland.pushygame.engine.events.FlowerEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Flower extends InteractableEntity {

    private final EntityType entityType;

    public Flower(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType == EntityType.FLOWER_RED ? R.drawable.flower_red : R.drawable.flower);
        this.entityType = entityType;
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        game.execute(new FlowerEvent(this, direction));
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return true;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
