package markus.wieland.pushygame.ui;

public class InventoryItem {

    private int drawable;
    private int amount;

    public InventoryItem(int drawable, int amount) {
        this.drawable = drawable;
        this.amount = amount;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
