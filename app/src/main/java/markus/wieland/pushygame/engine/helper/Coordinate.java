package markus.wieland.pushygame.engine.helper;

public class Coordinate {

    private int x;
    private int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Coordinate getNextCoordinate(Direction direction) {
        int nextX = x + direction.getDirectionChangeVertical();
        int nextY = y + direction.getDirectionChangeHorizontal();
        return new Coordinate(nextX, nextY);
    }

    public Direction getDirection(Coordinate to) {
        int differenceX = to.getX() - x ;
        int differenceY = to.getY() - y;

        if (differenceX <= -1 && differenceY == 0) return Direction.NORTH;
        if (differenceX >= 1 && differenceY == 0) return Direction.SOUTH;
        if (differenceX == 0 && differenceY <= -1) return Direction.WEST;
        if (differenceX == 0 && differenceY >= 1) return Direction.EAST;

        return null;
    }
}
