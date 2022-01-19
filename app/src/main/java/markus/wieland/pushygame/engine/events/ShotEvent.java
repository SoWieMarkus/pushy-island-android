package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.interactable.SlingShot;
import markus.wieland.pushygame.engine.entity.movable.Leaf;
import markus.wieland.pushygame.engine.entity.movable.Shell;
import markus.wieland.pushygame.engine.entity.statics.Tree;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.level.TileMapBuilder;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class ShotEvent extends Event {

    // because of the leafs its possible to create a infinite loop
    // so I break after MAX_STEPS steps
    private static final int MAX_STEPS = 1000;

    private final SlingShot slingShot;

    public ShotEvent(SlingShot slingShot) {
        super();
        this.slingShot = slingShot;
    }

    @Override
    public void execute() {
        Direction direction = Direction.NORTH;
        Coordinate nextCoordinate = slingShot.getCoordinate().getNextCoordinate(direction);
        int count = 0;

        while (!game.getEntityManager().isNotInsideField(nextCoordinate) && count < MAX_STEPS) {
            Terrain terrain = game.getTerrainManager().getObject(nextCoordinate);
            Entity entity = game.getEntityManager().getObject(nextCoordinate);

            if (entity instanceof Leaf) direction = ((Leaf) entity).getDirection();
            if (entity instanceof Shell) {
                ((Shell) entity).setOpen(true);
                game.getEntityManager().invalidate(entity);
            }

            nextCoordinate = nextCoordinate.getNextCoordinate(direction);
            if ((entity != null && entity.destroysFlyingStone()) || terrain.destroysFlyingStone()) {
                if (entity instanceof Tree && !game.getEntityManager().isNotInsideField(nextCoordinate)
                        && game.getTerrainManager().getObject(nextCoordinate).getElevation() == Terrain.ELEVATION_SAND)
                    game.getEntityManager().setObject(nextCoordinate, TileMapBuilder.build(EntityType.COCONUT, nextCoordinate));
                break;
            }
            count++;
        }

    }
}
