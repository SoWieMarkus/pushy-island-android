package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.terrain.InvisibleWater;

public class ShowInvisibleWaterBlocksEvent extends Event {

    private final boolean visible;

    public ShowInvisibleWaterBlocksEvent(boolean visible) {
        super();
        this.visible = visible;
    }

    @Override
    public void execute() {
        for (InvisibleWater invisibleWater : game.getTerrainManager().getOfType(InvisibleWater.class)) {
            invisibleWater.setVisible(!visible);
            game.getTerrainManager().invalidate(invisibleWater);
        }
    }
}
