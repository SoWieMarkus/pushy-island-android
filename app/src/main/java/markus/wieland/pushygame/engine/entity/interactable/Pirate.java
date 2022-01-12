package markus.wieland.pushygame.engine.entity.interactable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.entity.GameFinishEntity;
import markus.wieland.pushygame.engine.entity.RewardEntity;
import markus.wieland.pushygame.engine.entity.collectible.Coin;
import markus.wieland.pushygame.engine.entity.interactable.rewards.PirateReward;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Pirate extends RewardEntity implements GameFinishEntity {

    public Pirate(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType, Coin.class);
    }

    @Override
    protected CollectibleEntity createRewardItem() {
        return new PirateReward();
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return direction == Direction.WEST;
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        super.executeInteraction(direction, game);
        if (gotReward) game.getEntityManager().remove(this);
    }

    @Override
    public boolean check(Game game) {
        return game.getInventory().getAmount(PirateReward.class) > 0;
    }
}
