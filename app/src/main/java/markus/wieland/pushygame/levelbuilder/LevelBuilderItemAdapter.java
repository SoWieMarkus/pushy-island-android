package markus.wieland.pushygame.levelbuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import markus.wieland.defaultappelements.uielements.adapter.DefaultAdapter;
import markus.wieland.defaultappelements.uielements.adapter.DefaultViewHolder;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemInteractListener;
import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.helper.Field;

public class LevelBuilderItemAdapter<F extends Field> extends DefaultAdapter<F, LevelBuilderItemAdapter.LevelBuilderItemViewHolder<F>> {


    public LevelBuilderItemAdapter(OnItemInteractListener<F> onItemInteractListener) {
        super(onItemInteractListener);
    }

    @NonNull
    @Override
    public LevelBuilderItemViewHolder<F> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LevelBuilderItemViewHolder<>(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_builder, parent, false));
    }

    public static class LevelBuilderItemViewHolder<F extends Field> extends DefaultViewHolder<F> {

        private ImageView icon;

        public LevelBuilderItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindViews() {
            icon = findViewById(R.id.item_level_builder_icon);
        }

        @Override
        public void bindItemToViewHolder(F field) {
            icon.setImageResource(field.getDrawable());
        }
    }
}
