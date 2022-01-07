package markus.wieland.pushygame.engine.helper;

public enum Direction {

    NORTH(-1, 0),
    SOUTH(1, 0),
    EAST(0, 1),
    WEST(0, -1);

    private final int vertical;
    private final int horizontal;

    Direction(int vertical, int horizontal) {
        this.vertical = vertical;
        this.horizontal = horizontal;
    }

    public Direction getOppositeDirection() {
        switch (this) {
            case EAST:
                return Direction.WEST;
            case WEST:
                return Direction.EAST;
            case NORTH:
                return Direction.SOUTH;
            default:
                return Direction.NORTH;
        }
    }

    public int getDirectionChangeVertical() {
        return vertical;
    }

    public int getDirectionChangeHorizontal() {
        return horizontal;
    }

}
