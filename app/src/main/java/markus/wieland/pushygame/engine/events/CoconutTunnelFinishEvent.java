package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.movable.Coconut;
import markus.wieland.pushygame.engine.terrain.pressure.CoconutTunnelFinish;
import markus.wieland.pushygame.engine.terrain.Hole;

public class CoconutTunnelFinishEvent extends Event {

    private static boolean executed = false;

    public static void setExecuted(boolean executed) {
        CoconutTunnelFinishEvent.executed = executed;
    }

    public CoconutTunnelFinishEvent() {
        super();
    }

    @Override
    public void execute() {
        if (executed) return;

        for (CoconutTunnelFinish coconutTunnelFinish : game.getTerrainManager().getSubListOfPressurePlates(CoconutTunnelFinish.class)) {
            if (!(game.getEntityManager().getObject(coconutTunnelFinish) instanceof Coconut))
                return;
        }
        for (Hole hole : game.getTerrainManager().getOfType(Hole.class)) {
            hole.setFilled();
            game.getTerrainManager().invalidate(hole);
        }
        setExecuted(true);
    }
}
