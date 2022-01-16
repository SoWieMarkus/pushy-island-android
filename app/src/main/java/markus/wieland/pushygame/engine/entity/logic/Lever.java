package markus.wieland.pushygame.engine.entity.logic;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.InteractableEntity;
import markus.wieland.pushygame.engine.entity.collectible.Energy;
import markus.wieland.pushygame.engine.events.LogicEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Lever extends InteractableEntity implements LogicOutput {

    private boolean active;

    public Lever(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    public PortType getPortType(Direction direction) {
        return PortType.OUTPUT;
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        if (!game.getInventory().get(Energy.class, 1)) return;

        this.active = !active;
        game.getEntityManager().invalidate(this);

        for (Direction direction1 : Direction.class.getEnumConstants()) {
            game.execute(new LogicEvent(getCoordinate().getNextCoordinate(direction1), direction1));
        }

    }

    @Override
    public int getDrawable() {
        return active ? R.drawable.lever_active : super.getDrawable();
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return true;
    }

    @Override
    public boolean isOutputActive(Game game) {
        return active;
    }

    @Override
    public boolean isOutput(Direction direction) {
        return true;
    }
}
