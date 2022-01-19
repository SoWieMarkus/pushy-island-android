package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.terrain.Cable;

public class UpdateCableEvent extends Event {
    @Override
    public void execute() {
        for (Cable cable : game.getTerrainManager().getOfType(Cable.class)) {
            cable.updateDrawables(getGame());
        }
    }
}
