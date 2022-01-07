package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.movable.Leaf;
import markus.wieland.pushygame.engine.helper.Direction;

public class ChangeLeafDirectionEvent extends Event {
    private final Direction direction;

    public ChangeLeafDirectionEvent(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void execute() {
        for (Leaf leaf : game.getEntityManager().getOfType(Leaf.class)) {
            leaf.setDirection(direction);
            game.getEntityManager().invalidate(leaf);
        }
    }
}
