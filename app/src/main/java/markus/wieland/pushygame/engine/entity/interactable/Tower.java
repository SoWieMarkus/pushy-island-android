package markus.wieland.pushygame.engine.entity.interactable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.entity.RewardEntity;
import markus.wieland.pushygame.engine.entity.collectible.Pearl;
import markus.wieland.pushygame.engine.events.TowerEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Tower extends RewardEntity {

    public Tower(Coordinate coordinate) {
        super(coordinate, R.drawable.tower, Pearl.class);
        setNeedItemsAmount(1);
    }

    @Override
    protected CollectibleEntity createRewardItem() {
        return null;
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        super.executeInteraction(direction, game);
        if (gotReward) game.execute(new TowerEvent());
    }

    @Override
    public int getDrawable() {
        return gotReward ? R.drawable.tower_with_pearl : super.getDrawable();
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return true;
    }

    @Override
    public EntityType getType() {
        return EntityType.TOWER;
    }
}
