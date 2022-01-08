package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.events.StatueEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Statue extends MovableEntity {

    private final EntityType entityType;

    public Statue(Coordinate coordinate, EntityType entityType) {
        super(coordinate, R.drawable.statue_blue);
        this.entityType = entityType;
    }

    @Override
    public int getDrawable() {
        switch (entityType) {
            case STATUE_BLUE:
                return R.drawable.statue_blue;
            case STATUE_GREEN:
                return R.drawable.statue_green;
            default:
                return R.drawable.statue_red;
        }
    }

    // Never return true here so Pushy can't be moved! Pushy will be moved in the StatueEvent
    @Override
    public boolean move(Coordinate nextCoordinate, Game game) {
        if (isMovePossible(nextCoordinate, game)) {
            executeMove(nextCoordinate, game);
        }
        return false;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        game.execute(new StatueEvent(getCoordinate(), nextCoordinate));
    }

    @Override
    public EntityType getType() {
        return entityType;
    }
}
