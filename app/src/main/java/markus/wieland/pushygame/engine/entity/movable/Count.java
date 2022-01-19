package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.GameFinishEntity;
import markus.wieland.pushygame.engine.events.CountEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Count extends MovableEntity implements GameFinishEntity {

    private final int countValue;
    private boolean isUncovered;

    public Count(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
        this.isUncovered = true;
        switch (entityType) {
            case COUNT_TWO:
                countValue = 2;
                break;
            case COUNT_THREE:
                countValue = 3;
                break;
            case COUNT_FOUR:
                countValue = 4;
                break;
            case COUNT_FIVE:
                countValue = 5;
                break;
            default:
                countValue = 1;
                break;
        }
    }

    @Override
    public int getDrawable() {
        return isUncovered ? super.getDrawable() : R.drawable.count_hidden;
    }

    public boolean isUncovered() {
        return isUncovered;
    }

    public void setUncovered(boolean uncovered) {
        isUncovered = uncovered;
    }

    @Override
    public boolean isMovePossible(Coordinate nextCoordinate, Game game) {
        return super.isMovePossible(nextCoordinate, game) && !isUncovered;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        super.executeMove(nextCoordinate, game);
        isUncovered = true;
        game.getEntityManager().invalidate(this);
        game.execute(new CountEvent(countValue));
    }

    @Override
    public boolean check(Game game) {
        return isUncovered();
    }
}
