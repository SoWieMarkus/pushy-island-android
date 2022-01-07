package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.collectible.Seed;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.terrain.Farm;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class Pushy extends MovableEntity {

    private Direction facing;

    public Pushy(Coordinate coordinate) {
        super(coordinate, R.drawable.pushy);
        facing = Direction.NORTH;
    }

    public Direction getFacing() {
        return facing;
    }

    @Override
    public int getDrawable() {
        switch (facing) {
            case SOUTH:
                return R.drawable.pushy_south;
            case WEST:
                return R.drawable.pushy_west;
            case EAST:
                return R.drawable.pushy_east;
            default:
                return R.drawable.pushy;
        }
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    @Override
    public boolean canCollectEntities() {
        return true;
    }

    @Override
    public boolean canMoveEntities() {
        return true;
    }

    @Override
    public boolean canInteractWithEntities() {
        return true;
    }

    @Override
    public boolean canSwimWithABoat() {
        return true;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        super.executeMove(nextCoordinate, game);
        Terrain terrain = game.getTerrainManager().getObject(this);
        if (terrain instanceof Farm && game.getInventory().get(Seed.class, 1)) {
            ((Farm) terrain).setHasSeed(true);
            game.getTerrainManager().invalidate(getCoordinate());
        }
    }
}
