package markus.wieland.pushygame.engine.entity.logic;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class CountDown extends Lamp {

    private static final int MAX_COUNT_DOWN_VALUE = 3;
    private static final int MIN_COUNT_DOWN_VALUE = 0;

    private int count;

    public CountDown(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
        this.count = MAX_COUNT_DOWN_VALUE;
    }

    @Override
    public int getDrawable() {
        switch (count) {
            case MIN_COUNT_DOWN_VALUE:
                return R.drawable.lamp_active;
            case 2:
                return R.drawable.count_down_two;
            case 1:
                return R.drawable.count_down_one;
            default:
                return R.drawable.count_down_three;
        }
    }

    @Override
    public void update(Game game) {
        boolean countDown = false;
        for (Direction direction : getPorts().getInputs())
            countDown = isInputActive(game, direction) || countDown;

        if (!countDown || count == MIN_COUNT_DOWN_VALUE) return;

        count--;

        if (count == MIN_COUNT_DOWN_VALUE) setActive(true);

        game.getEntityManager().invalidate(this);
    }

}
