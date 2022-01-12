package markus.wieland.pushygame.engine.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.events.InventoryEventListener;
import markus.wieland.pushygame.ui.game.InventoryItem;

public class Inventory {

    private InventoryEventListener inventoryEventListener;

    private final HashMap<Class<? extends CollectibleEntity>, List<CollectibleEntity>> inventoryItems;

    public Inventory() {
        this.inventoryItems = new HashMap<>();
    }

    public void setInventoryEventListener(InventoryEventListener inventoryEventListener) {
        this.inventoryEventListener = inventoryEventListener;

    }

    public void add(CollectibleEntity collectibleEntity) {

        if (inventoryItems.get(collectibleEntity.getClass()) == null) {
            inventoryItems.put(collectibleEntity.getClass(), new ArrayList<>());
        }
        Objects.requireNonNull(inventoryItems.get(collectibleEntity.getClass())).add(collectibleEntity);
        if (inventoryEventListener != null) inventoryEventListener.onInventoryChanged();
    }

    public boolean get(CollectibleEntity entity, int amount) {
        return get(entity.getClass(), amount);
    }

    public boolean get(Class<? extends CollectibleEntity> type, int amount) {
        if (!has(type, amount)) return false;
        if (amount > 0) Objects.requireNonNull(inventoryItems.get(type)).subList(0, amount).clear();
        if (inventoryEventListener != null) inventoryEventListener.onInventoryChanged();
        return true;
    }

    public boolean has(Class<? extends CollectibleEntity> type, int amount) {
        if (inventoryItems.get(type) == null) return false;
        return inventoryItems.get(type).size() >= amount;
    }

    public int getAmount(Class<? extends CollectibleEntity> type) {
        if (inventoryItems.get(type) == null) return 0;
        return inventoryItems.get(type).size();
    }

    public List<InventoryItem> getInventoryItems() {
        List<InventoryItem> inventoryItemsList = new ArrayList<>();

        for (Map.Entry<Class<? extends CollectibleEntity>, List<CollectibleEntity>> entry: inventoryItems.entrySet()) {
            if (entry.getValue().isEmpty())continue;
            inventoryItemsList.add(new InventoryItem(entry.getValue().get(0).getDrawable(), entry.getValue().size()));
        }
        
        return inventoryItemsList;
    }
}
