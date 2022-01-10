package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.TileMapBuilder;
import markus.wieland.pushygame.engine.terrain.Water;

public class Box extends MovableEntity {

    public Box(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    public boolean isPushableIntoWater() {
        return true;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        super.executeMove(nextCoordinate, game);
        if (game.getTerrainManager().getObject(this) instanceof Water) {
            game.getTerrainManager().setObject(getCoordinate(), TileMapBuilder.build(TerrainType.BOX_WATER, getCoordinate()));
            game.getEntityManager().remove(this);
        }
    }

    @Override
    public boolean destroysFlyingStone() {
        return true;
    }

}
