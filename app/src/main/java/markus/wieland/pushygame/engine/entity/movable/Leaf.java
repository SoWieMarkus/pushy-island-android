package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Leaf  extends MovableEntity{

    private Direction direction;

    public Leaf(Coordinate coordinate, EntityType entityType) {
        super(coordinate, R.drawable.leaf_down);
        switch (entityType) {
            case LEAF_DOWN:
                direction = Direction.SOUTH;
                break;
            case LEAF_UP:
                direction = Direction.NORTH;
                break;
            case LEAF_LEFT:
                direction = Direction.WEST;
                break;
            default:
                direction = Direction.EAST;
                break;
        }
    }

    @Override
    public int getDrawable() {
        switch (direction) {
            case SOUTH:
                return R.drawable.leaf_down;
            case NORTH:
                return R.drawable.leaf_up;
            case WEST:
                return R.drawable.leaf_left;
            default:
                return R.drawable.leaf_right;
        }
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
