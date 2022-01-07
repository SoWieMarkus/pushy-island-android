package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class PushShell extends MovableEntity {

    public static final int HAPPY = 2;
    public static final int MEDIUM = 1;
    public static final int UNHAPPY = 0;

    private int count = 0;

    public PushShell(Coordinate coordinate, EntityType entityType) {
        super(coordinate, R.drawable.shell_1);
        switch (entityType) {
            case SHELL_3:
                count = HAPPY;
                break;
            case SHELL_2:
                count = MEDIUM;
                break;
            default:
                count = UNHAPPY;
                break;
        }
    }

    public int getCount() {
        return count;
    }

    @Override
    public int getDrawable() {
        switch (count) {
            case HAPPY:
                return R.drawable.shell_3;
            case MEDIUM:
                return R.drawable.shell_2;
            default:
                return R.drawable.shell_1;
        }

    }

    private void countUp() {
        count = (count + 1) % 3;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        countUp();
        super.executeMove(nextCoordinate, game);
    }
}
