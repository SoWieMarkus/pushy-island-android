package markus.wieland.pushygame.ui.customlevel.levelbuilder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import markus.wieland.defaultappelements.uielements.adapter.DefaultAdapter;
import markus.wieland.defaultappelements.uielements.adapter.DefaultViewHolder;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemClickListener;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemInteractListener;
import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.level.Type;

public class LevelBuilderItemAdapter extends DefaultAdapter<Type, LevelBuilderItemAdapter.LevelBuilderItemViewHolder> {

    private static Type selected;

    public LevelBuilderItemAdapter(OnItemInteractListener<Type> onItemInteractListener) {
        super(onItemInteractListener);
    }

    @NonNull
    @Override
    public LevelBuilderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LevelBuilderItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_builder, parent, false));
    }

    public void select(Type type) {
        setSelected(type);
        notifyDataSetChanged();
    }

    private static void setSelected(Type type) {
        selected = type;
    }

    @Override
    public OnItemClickListener<Type> getOnItemInteractListener() {
        return (OnItemClickListener<Type>) super.getOnItemInteractListener();
    }

    public class LevelBuilderItemViewHolder extends DefaultViewHolder<Type> {

        private ImageView icon;

        public LevelBuilderItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindViews() {
            icon = findViewById(R.id.item_level_builder_icon);
        }

        @Override
        public void bindItemToViewHolder(Type type) {
            icon.setImageResource(type.getDrawable());
            itemView.setBackgroundColor(type == selected ? Color.RED : Color.TRANSPARENT);
            icon.setOnClickListener(view -> getOnItemInteractListener().onClick(type));
        }

    }
}
