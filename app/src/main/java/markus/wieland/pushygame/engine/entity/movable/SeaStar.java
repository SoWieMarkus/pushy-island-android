package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class SeaStar extends MovableEntity {

    public SeaStar(Coordinate coordinate) {
        super(coordinate, R.drawable.seastar);
    }

    @Override
    public boolean isPushableIntoWater() {
        return true;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        super.executeMove(nextCoordinate, game);
        if (game.getTerrainManager().getObject(this).getElevation() == Terrain.ELEVATION_WATER) {
            game.getEntityManager().remove(this);
        }
    }

    @Override
    public EntityType getType() {
        return EntityType.SEA_STAR;
    }
}
