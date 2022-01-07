package markus.wieland.pushygame.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import markus.wieland.defaultappelements.uielements.adapter.DefaultAdapter;
import markus.wieland.defaultappelements.uielements.adapter.DefaultViewHolder;
import markus.wieland.pushygame.R;

public class InventoryAdapter extends DefaultAdapter<InventoryItem, InventoryAdapter.InventoryViewHolder> {

    public InventoryAdapter() {
        super(null);
    }

    @NonNull
    @Override
    public InventoryAdapter.InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InventoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false));
    }

    public static class InventoryViewHolder extends DefaultViewHolder<InventoryItem> {

        private TextView amount;
        private ImageView icon;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindViews() {
            amount = findViewById(R.id.item_inventory_amount);
            icon = findViewById(R.id.item_inventory_icon);
        }

        @Override
        public void bindItemToViewHolder(InventoryItem inventoryItem) {
            amount.setText(String.valueOf(inventoryItem.getAmount()));
            icon.setImageResource(inventoryItem.getDrawable());
        }
    }
}
