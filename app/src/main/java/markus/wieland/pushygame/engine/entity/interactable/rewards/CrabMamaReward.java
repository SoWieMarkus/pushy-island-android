package markus.wieland.pushygame.engine.entity.interactable.rewards;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.level.EntityType;

public class CrabMamaReward extends CollectibleEntity {

    public CrabMamaReward() {
        super(null, EntityType.CRAB_MOTHER);
    }

    // This is just a theoretical item. This item can not be placed so we don't need a type for it
    @Override
    public EntityType getType() {
        return null;
    }

    @Override
    public int getDrawable() {
        return R.drawable.crab_mother_happy;
    }
}
