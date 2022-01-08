package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.movable.Coconut;
import markus.wieland.pushygame.engine.events.CoconutTunnelFinishEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.TerrainType;

public class CoconutTunnelFinish extends PressurePlateTerrain {

    private Entity lastEntity;

    public CoconutTunnelFinish(Coordinate coordinate) {
        super(coordinate, R.drawable.coconut_tunnel_finish);
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (lastEntity == entity) return;
        lastEntity = entity;
        if (entity instanceof Coconut) {
            game.execute(new CoconutTunnelFinishEvent());
        }
    }

    @Override
    public boolean isCoconutTunnel() {
        return true;
    }

    @Override
    public TerrainType getType() {
        return TerrainType.COCONUT_TUNNEL_FINISH;
    }

    @Override
    public boolean coconutCanEnterFromTunnel() {
        return true;
    }
}
