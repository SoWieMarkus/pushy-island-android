package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Leaf  extends MovableEntity{

    private final Direction direction;

    public Leaf(Coordinate coordinate, EntityType entityType) {
        super(coordinate, R.drawable.leaf_down);
        switch (entityType) {
            case LEAF_DOWN:
                setDrawable(R.drawable.leaf_down);
                direction = Direction.SOUTH;
                break;
            case LEAF_UP:
                setDrawable(R.drawable.leaf_up);
                direction = Direction.NORTH;
                break;
            case LEAF_LEFT:
                setDrawable(R.drawable.leaf_left);
                direction = Direction.WEST;
                break;
            default:
                setDrawable(R.drawable.leaf_right);
                direction = Direction.EAST;
                break;
        }
    }

    public Direction getDirection() {
        return direction;
    }
}
