package markus.wieland.pushygame.engine.entity.logic;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Direction;

public interface LogicOutput {

    boolean isOutputActive(Game game);

    boolean isOutput(Direction direction);

    static int drawableByDirection(Direction direction){
        direction = direction.getOppositeDirection();
        switch (direction) {
            case NORTH:
                return R.drawable.output_north;
            case WEST:
                return R.drawable.output_west;
            case EAST:
                return R.drawable.output_east;
            default:
                return R.drawable.output_south;
        }
    }

}
