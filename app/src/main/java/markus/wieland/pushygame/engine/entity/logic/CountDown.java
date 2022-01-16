package markus.wieland.pushygame.engine.entity.logic;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class CountDown extends Lamp {

    private int count;

    public CountDown(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
        this.count = 3;
    }

    @Override
    public int getDrawable() {
        switch (count) {
            case 0:
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
        for (Direction direction : getPorts().getInputs()) {
            countDown = isInputActive(game, direction) || countDown;
        }

        if (!countDown || count == 0) return;

        count--;

        if (count == 0) {
            setActive(true);
        }

        game.getEntityManager().invalidate(this);
    }

}
