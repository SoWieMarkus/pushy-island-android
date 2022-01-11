package markus.wieland.pushygame.ui.customlevel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import markus.wieland.defaultappelements.uielements.adapter.DefaultAdapter;
import markus.wieland.defaultappelements.uielements.adapter.DefaultViewHolder;
import markus.wieland.defaultappelements.uielements.adapter.QueryableAdapter;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemInteractListener;
import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;

public class CustomLevelAdapter extends QueryableAdapter<Long, LevelDisplayItem, CustomLevelAdapter.CustomLevelViewHolder> {

    public CustomLevelAdapter(CustomLevelInteractionListener customLevelInteractionListener) {
        super(customLevelInteractionListener);
    }

    @NonNull
    @Override
    public CustomLevelAdapter.CustomLevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomLevelAdapter.CustomLevelViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_custom, parent, false));
    }

    @Override
    public CustomLevelInteractionListener getOnItemInteractListener() {
        return (CustomLevelInteractionListener)super.getOnItemInteractListener();
    }

    public class CustomLevelViewHolder extends DefaultViewHolder<LevelDisplayItem> {

        private ImageButton play;
        private ImageButton edit;
        private ImageButton delete;
        private TextView name;
        private ImageButton share;

        public CustomLevelViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindViews() {
            play = findViewById(R.id.item_level_custom_play);
            share = findViewById(R.id.item_level_custom_share);
            edit = findViewById(R.id.item_level_custom_edit);
            name = findViewById(R.id.item_level_custom_name);
            delete = findViewById(R.id.item_level_custom_delete);
        }

        @Override
        public void bindItemToViewHolder(LevelDisplayItem levelDisplayItem) {
            name.setText(levelDisplayItem.getName());
            play.setOnClickListener(view -> getOnItemInteractListener().onPlay(levelDisplayItem));
            edit.setOnClickListener(view -> getOnItemInteractListener().onEdit(levelDisplayItem));
            share.setOnClickListener(view -> getOnItemInteractListener().onShare(levelDisplayItem));
            share.setVisibility(levelDisplayItem.isSolved() ? View.VISIBLE : View.GONE);
            delete.setOnClickListener(view -> getOnItemInteractListener().onDelete(levelDisplayItem));
            //TODO text der zeigt das noch validiert werden muss
        }
    }
}
