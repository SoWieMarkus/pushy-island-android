package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Leaf extends MovableEntity {

    public Leaf(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    public void setDirection(Direction direction) {
        switch (direction) {
            case NORTH:
                setType(EntityType.LEAF_UP);
                break;
            case SOUTH:
                setType(EntityType.LEAF_DOWN);
                break;
            case EAST:
                setType(EntityType.LEAF_RIGHT);
                break;
            default:
                setType(EntityType.LEAF_LEFT);
                break;
        }
    }

    public Direction getDirection() {
        switch (getType()) {
            case LEAF_DOWN:
                return Direction.SOUTH;
            case LEAF_UP:
                return Direction.NORTH;
            case LEAF_LEFT:
                return Direction.WEST;
            default:
                return Direction.EAST;
        }
    }

}
