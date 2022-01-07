package markus.wieland.pushygame.engine.helper;

import androidx.annotation.DrawableRes;

public abstract class Field {

    private Coordinate coordinate;

    @DrawableRes
    private int drawable;

    public Field(Coordinate coordinate, int drawable) {
        this.coordinate = coordinate;
        this.drawable = drawable;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public boolean destroysFlyingStone() {
        return false;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = new Coordinate(coordinate.getX(), coordinate.getY());
    }


}
