package markus.wieland.pushygame.engine;

import markus.wieland.pushygame.engine.entity.CollectibleEntity;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.collectible.Coin;
import markus.wieland.pushygame.engine.entity.collectible.Key;
import markus.wieland.pushygame.engine.entity.interactable.CrabMama;
import markus.wieland.pushygame.engine.entity.interactable.Pirate;
import markus.wieland.pushygame.engine.entity.collectible.CrabBaby;
import markus.wieland.pushygame.engine.entity.movable.Pushy;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.helper.Inventory;
import markus.wieland.pushygame.engine.helper.Manager;
import markus.wieland.pushygame.engine.helper.Matrix;
import markus.wieland.pushygame.ui.game.PushyFieldView;

public class EntityManager extends Manager<Entity, PushyFieldView<Entity>> {

    private Pushy pushy;
    private CrabMama crabMama;
    private Pirate pirate;

    public EntityManager(Matrix<PushyFieldView<Entity>> pushyFieldViews) {
        super(pushyFieldViews);
        Inventory inventory = new Inventory();
        for (PushyFieldView<Entity> pushyFieldView : pushyFieldViews) {
            Entity entity = pushyFieldView.get();
            if (entity instanceof Pushy) pushy = (Pushy) entity;
            if (entity instanceof CollectibleEntity)
                inventory.add((CollectibleEntity) entity);
            if (entity instanceof CrabMama)
                crabMama = (CrabMama) entity;
            if (entity instanceof Pirate) {
                pirate = (Pirate) entity;
            }
        }

        if (crabMama != null) crabMama.setNeedItemsAmount(inventory.getAmount(CrabBaby.class));
        if (pirate != null)
            pirate.setNeedItemsAmount(inventory.getAmount(Coin.class) + inventory.getAmount(Key.class));

    }


    public void setPushy(Pushy pushy) {
        this.pushy = pushy;
    }

    public Pushy getPushy() {
        return pushy;
    }

}
