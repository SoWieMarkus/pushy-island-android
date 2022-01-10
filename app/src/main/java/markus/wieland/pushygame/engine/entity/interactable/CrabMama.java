package markus.wieland.pushygame.engine.entity.interactable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.entity.RewardEntity;
import markus.wieland.pushygame.engine.entity.collectible.CrabBaby;
import markus.wieland.pushygame.engine.entity.interactable.rewards.CrabMamaReward;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class CrabMama extends RewardEntity {

    public CrabMama(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType, CrabBaby.class);
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return direction == Direction.SOUTH;
    }

    @Override
    public int getDrawable() {
        return gotReward ? R.drawable.crab_mother_happy : super.getDrawable();
    }

    @Override
    protected CollectibleEntity createRewardItem() {
        return new CrabMamaReward();
    }

    @Override
    public EntityType getType() {
        return EntityType.CRAB_MOTHER;
    }
}
