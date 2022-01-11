package markus.wieland.pushygame.ui.game;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class PushyView extends GridView {

    public PushyView(Context context) {
        super(context);
    }

    public PushyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PushyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }


}
