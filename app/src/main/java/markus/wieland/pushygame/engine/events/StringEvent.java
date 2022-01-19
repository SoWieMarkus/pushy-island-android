package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.statics.String;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.terrain.ChangeableFlower;

public class StringEvent extends Event {

    private static Direction stringDirection;
    private static boolean isStringActive;

    public static Direction getStringDirection() {
        return stringDirection;
    }

    public static void setStringDirection(Direction stringDirection) {
        StringEvent.stringDirection = stringDirection;
    }

    public static boolean isStringActive() {
        return isStringActive;
    }

    public static void setIsStringActive(boolean isStringActive) {
        StringEvent.isStringActive = isStringActive;
    }

    @Override
    public void execute() {
        if (isStringActive()) {
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

            setIsStringActive(false);
            return;
        }
        setStringDirection(null);
        setIsStringActive(true);
    }

}
