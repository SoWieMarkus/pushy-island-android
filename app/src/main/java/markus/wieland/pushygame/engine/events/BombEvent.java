package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.movable.Bomb;

public class BombEvent extends Event {

    public BombEvent() {
        super();
    }

    @Override
    public void execute() {
        for (Bomb bomb : game.getEntityManager().getOfType(Bomb.class)) {
            bomb.explode(game);
        }
    }
}
