package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.statics.String;
import markus.wieland.pushygame.engine.terrain.ChangeableFlower;

public class StringEvent extends Event{

    @Override
    public void execute() {
        if (game.getEntityManager().isStringActive()) {
            for (ChangeableFlower changeableFlower : game.getTerrainManager().getOfType(ChangeableFlower.class)) {
                Entity entity = game.getEntityManager().getObject(changeableFlower);
                if (entity instanceof String) {
                    changeableFlower.change();
                    game.getTerrainManager().invalidate(changeableFlower);
                }
            }

            for (String string : game.getEntityManager().getOfType(String.class)) {
                game.getEntityManager().remove(string);
            }

            game.getEntityManager().setStringActive(false);
            return;
        }
        game.getEntityManager().setStringDirection(null);
        game.getEntityManager().setStringActive(true);
    }

}
