package markus.wieland.pushygame.ui.game;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import markus.wieland.pushygame.engine.helper.Matrix;

public class PushyGridAdapter<T extends View> extends BaseAdapter {

    private final Matrix<T> matrix;

    public PushyGridAdapter(Matrix<T> matrix) {
        this.matrix = matrix;
    }

    @Override
    public int getCount() {
        return matrix.getSizeX() * matrix.getSizeY();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return matrix.get(i);
    }
}
