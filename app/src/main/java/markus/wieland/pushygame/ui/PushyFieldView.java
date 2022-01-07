package markus.wieland.pushygame.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Field;

public class PushyFieldView<T extends Field> extends View {

    private T field;

    public PushyFieldView(Context context, T field) {
        super(context);
        this.field = field;
    }

    public PushyFieldView(Context context) {
        super(context);
    }

    public PushyFieldView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PushyFieldView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void set(T field) {
        this.field = field;
        invalidate();
    }

    public T get() {
        return field;
    }

    @Override
    protected void onMeasure(int w, int h) {
        super.onMeasure(w, w);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        draw(field == null ? R.drawable.no_entity : field.getDrawable(), canvas);
    }

    private void draw(@DrawableRes int drawableRes, Canvas canvas) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), drawableRes);
        assert drawable != null;
        drawable.setBounds(0, 0, getWidth(), getHeight());
        drawable.draw(canvas);
    }



}
