package markus.wieland.pushygame.engine.helper;

import androidx.annotation.DrawableRes;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.level.Type;

public abstract class Field {

    private Coordinate coordinate;

    private Type type;

    public Field(Coordinate coordinate, Type type) {
        this.coordinate = coordinate;

        this.type = type;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getDrawable() {
        return type == null ? R.drawable.no_entity : type.getDrawable();
    }

    public boolean destroysFlyingStone() {
        return false;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = new Coordinate(coordinate.getX(), coordinate.getY());
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }


}
