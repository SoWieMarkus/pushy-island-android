package markus.wieland.pushygame.ui.customlevel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.R;

public class ExpandableFloatingActionButton extends FloatingActionButton implements View.OnClickListener {

    private Animation flyIn;
    private Animation flyOut;
    private Animation rotateOpen;
    private Animation rotateClose;

    private boolean open;

    private final List<FloatingActionButton> floatingActionButtons = new ArrayList<>();

    public ExpandableFloatingActionButton(@NonNull Context context) {
        super(context, null);
    }

    public ExpandableFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ExpandableFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public void initialize(FloatingActionButton... floatingActionButtons){
        flyIn = AnimationUtils.loadAnimation(getContext(), R.anim.animation_fly_in);
        flyOut = AnimationUtils.loadAnimation(getContext(), R.anim.animation_fly_out);
        rotateOpen = AnimationUtils.loadAnimation(getContext(), R.anim.animation_rotate);
        rotateClose = AnimationUtils.loadAnimation(getContext(), R.anim.animation_rotate_close);
        this.floatingActionButtons.clear();
        add(floatingActionButtons);
        open = false;
        setOnClickListener(this);
    }

    public void add(FloatingActionButton... floatingActionButtons) {
        for (FloatingActionButton floatingActionButton : floatingActionButtons) {
            if (this.floatingActionButtons.contains(floatingActionButton)) return;
            this.floatingActionButtons.add(floatingActionButton);
            floatingActionButton.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View view) {
        open = !open;
        for (FloatingActionButton floatingActionButton : floatingActionButtons) {
            floatingActionButton.setVisibility(open ? VISIBLE : GONE);
            floatingActionButton.startAnimation(open ? flyIn : flyOut);
        }

        this.startAnimation(open ? rotateOpen : rotateClose);
    }
}
