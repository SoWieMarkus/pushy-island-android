package markus.wieland.pushygame.engine.helper;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.exceptions.FieldViewShouldNotBeNullException;
import markus.wieland.pushygame.engine.level.Type;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;
import markus.wieland.pushygame.ui.game.PushyFieldView;

public abstract class Manager<F extends Field, T extends PushyFieldView<F>> {

    private final Matrix<T> pushyFieldViews;

    public Manager(Matrix<T> pushyFieldViews) {
        this.pushyFieldViews = pushyFieldViews;
        initialize();
    }

    public Matrix<T> getPushyFieldViews() {
        return pushyFieldViews;
    }

    public boolean isNotInsideField(Coordinate coordinate) {
        return coordinate.getX() < 0 || coordinate.getY() < 0 || coordinate.getX() >= pushyFieldViews.getSizeX() || coordinate.getY() >= pushyFieldViews.getSizeY();
    }

    public F getObject(Coordinate coordinate) {
        if (isNotInsideField(coordinate)) return null;
        T view = getView(coordinate);
        if (pushyFieldViews.get(coordinate) == null) throw new FieldViewShouldNotBeNullException();
        return view.get();
    }

    public F getObject(Field field) {
        return getObject(field.getCoordinate());
    }

    public T getView(Coordinate coordinate) {
        if (isNotInsideField(coordinate)) return null;
        return pushyFieldViews.get(coordinate);
    }

    public void setObject(Coordinate coordinate, F field) {
        if (isNotInsideField(coordinate)) return;
        T view = getView(coordinate);
        if (view == null) throw new FieldViewShouldNotBeNullException();
        view.set(field);
        if (field != null) field.setCoordinate(coordinate);
        initialize();
    }

    public void swapFields(Coordinate from, Coordinate to) {
        F fieldFrom = getObject(from);
        F fieldTo = getObject(to);
        if (fieldFrom != null) fieldFrom.setCoordinate(to);
        if (fieldTo != null) fieldTo.setCoordinate(from);
        setObject(from, fieldTo);
        setObject(to, fieldFrom);
    }

    public void remove(F field) {
        if (isNotInsideField(field.getCoordinate())) return;
        T view = getView(field.getCoordinate());
        if (view == null) throw new FieldViewShouldNotBeNullException();
        view.set(null);
        initialize();
    }

    public void invalidate(F field) {
        invalidate(field.getCoordinate());
    }

    public void invalidate(Coordinate coordinate) {
        if (isNotInsideField(coordinate)) throw new FieldViewShouldNotBeNullException();
        pushyFieldViews.get(coordinate).invalidate();
    }

    public <G extends F> List<G> getOfType(Class<G> type) {
        List<G> filteredList = new ArrayList<>();
        for (PushyFieldView<F> pushyFieldView : pushyFieldViews) {
            if (type.isInstance(pushyFieldView.get())) filteredList.add((G) pushyFieldView.get());
        }
        return filteredList;
    }

    public List<F> getOfType(Type type) {
        List<F> filteredList = new ArrayList<>();
        for (PushyFieldView<F> pushyFieldView : pushyFieldViews) {
            if (pushyFieldView.get() == null) continue;
            if (type == pushyFieldView.get().getType()) filteredList.add(pushyFieldView.get());
        }
        return filteredList;
    }


    public List<F> getAll() {
        List<F> list = new ArrayList<>();
        for (PushyFieldView<F> pushyFieldView : pushyFieldViews) {
            if (pushyFieldView.get() == null) continue;
            list.add(pushyFieldView.get());
        }
        return list;
    }

    public Matrix<F> getAllAsMatrix() {
        Matrix<F> list = new Matrix<>(LevelBuilder.LEVEL_HEIGHT, LevelBuilder.LEVEL_WIDTH);
        for (PushyFieldView<F> pushyFieldView : pushyFieldViews) {
            if (pushyFieldView.get() == null) continue;
            list.set(pushyFieldView.get().getCoordinate(), pushyFieldView.get());
        }
        return list;
    }

    public void initialize(){}


}
