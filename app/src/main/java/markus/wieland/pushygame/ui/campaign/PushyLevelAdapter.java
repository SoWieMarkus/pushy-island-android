package markus.wieland.pushygame.ui.campaign;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import markus.wieland.defaultappelements.uielements.adapter.DefaultViewHolder;
import markus.wieland.defaultappelements.uielements.adapter.QueryableAdapter;
import markus.wieland.defaultappelements.uielements.adapter.iteractlistener.OnItemClickListener;
import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.level.LevelDisplayItem;

public class PushyLevelAdapter extends QueryableAdapter<Long, LevelDisplayItem, PushyLevelAdapter.PushyLevelViewHolder> {

    public PushyLevelAdapter(OnItemClickListener<LevelDisplayItem> onItemInteractListener) {
        super(onItemInteractListener);
    }

    @NonNull
    @Override
    public PushyLevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PushyLevelViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_campaign, parent, false));
    }

    @Override
    public OnItemClickListener<LevelDisplayItem> getOnItemInteractListener() {
        return (OnItemClickListener<LevelDisplayItem>) super.getOnItemInteractListener();
    }

    public class PushyLevelViewHolder extends DefaultViewHolder<LevelDisplayItem> {

        private TextView number;
        private TextView name;
        private CardView cardView;

        public PushyLevelViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindViews() {
            number = findViewById(R.id.item_level_campaign_number);
            name = findViewById(R.id.item_level_campaign_name);
            cardView = findViewById(R.id.item_level_campaign_card);
        }

        @Override
        public void bindItemToViewHolder(LevelDisplayItem levelDisplayItem) {
            number.setText(levelDisplayItem.getNumberAsString());
            name.setText(levelDisplayItem.getName());
            itemView.setOnClickListener(view -> getOnItemInteractListener().onClick(levelDisplayItem));
            cardView.setCardBackgroundColor(levelDisplayItem.isSolved() ? Color.rgb(50,205,50) : Color.GRAY);
        }
    }
}
