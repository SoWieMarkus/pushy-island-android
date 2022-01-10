package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TerrainType;
import markus.wieland.pushygame.engine.level.TileMapBuilder;
import markus.wieland.pushygame.engine.terrain.Farm;
import markus.wieland.pushygame.engine.terrain.Terrain;
import markus.wieland.pushygame.engine.terrain.WaterHole;

public class Bottle extends MovableEntity {

    private boolean isFilled;

    public Bottle(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
        isFilled = false;
    }

    public boolean isFilled() {
        return isFilled;
    }

    @Override
    public int getDrawable() {
        return isFilled ? R.drawable.bottle_with_water : super.getDrawable();
    }

    public void setFilled() {
        isFilled = true;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        super.executeMove(nextCoordinate, game);
        Terrain terrain = game.getTerrainManager().getObject(this);
        if (terrain instanceof Farm && ((Farm) terrain).hasSeed() && isFilled()) {
            game.getTerrainManager().setObject(getCoordinate(), TileMapBuilder.build(TerrainType.SPRING, getCoordinate()));
            game.getEntityManager().remove(this);
        }
        if (terrain instanceof WaterHole) {
            setFilled();
            game.getEntityManager().invalidate(this);
        }
    }

}
