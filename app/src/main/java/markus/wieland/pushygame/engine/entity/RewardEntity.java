package markus.wieland.pushygame.engine.entity;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.helper.Inventory;
import markus.wieland.pushygame.engine.level.EntityType;

public abstract class RewardEntity extends InteractableEntity {

    protected int needItemsAmount;
    protected int hasItemsAmount;
    protected boolean gotReward;
    protected final Class<? extends CollectibleEntity> returnType;

    public RewardEntity(Coordinate coordinate, EntityType entityType, Class<? extends CollectibleEntity> returnType) {
        super(coordinate, entityType);
        this.returnType = returnType;
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        if (gotReward) return;
        give(game.getInventory());
        CollectibleEntity collectibleEntity = getReward();
        if (collectibleEntity != null) game.getInventory().add(collectibleEntity);
        game.getEntityManager().invalidate(this);
    }

    protected boolean canGetReward() {
        return hasItemsAmount >= needItemsAmount  && !gotReward;
    }

    protected abstract CollectibleEntity createRewardItem();

    protected CollectibleEntity getReward() {
        if (canGetReward()) {
            gotReward = true;
            return createRewardItem();
        }
        return null;
    }

    public void give(Inventory inventory) {
        int amountInInventory = inventory.getAmount(returnType);
        if (amountInInventory == 0) return;
        if (amountInInventory > needItemsAmount) amountInInventory = needItemsAmount;
        inventory.get(returnType, amountInInventory);
        hasItemsAmount += amountInInventory;
    }

    public int getNeedItemsAmount() {
        return needItemsAmount;
    }

    public void setNeedItemsAmount(int needItemsAmount) {
        this.needItemsAmount = needItemsAmount;
    }

    public int getHasItemsAmount() {
        return hasItemsAmount;
    }

    public void setHasItemsAmount(int hasItemsAmount) {
        this.hasItemsAmount = hasItemsAmount;
    }

    public boolean gotReward() {
        return gotReward;
    }


    public Class<? extends CollectibleEntity> getRewardType() {
        return returnType;
    }

}
