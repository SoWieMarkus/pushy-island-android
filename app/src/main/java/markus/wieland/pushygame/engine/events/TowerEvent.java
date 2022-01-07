package markus.wieland.pushygame.engine.events;

import java.util.List;

import markus.wieland.pushygame.engine.entity.interactable.Tower;

public class TowerEvent extends Event {

    public TowerEvent() {
        super();
    }

    @Override
    public void execute() {
        List<Tower> towers = game.getEntityManager().getOfType(Tower.class);
        for (Tower tower : towers) {
            if (!tower.gotReward()) return;
        }
        for (Tower tower : towers) {
            game.getEntityManager().remove(tower);
        }
    }
}
