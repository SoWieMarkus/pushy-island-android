package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.interactable.Flower;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.TileMapBuilder;
import markus.wieland.pushygame.engine.terrain.FlowerFinish;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class FlowerEvent extends Event {

    private final Flower flower;
    private final Direction direction;

    public FlowerEvent(Flower flower, Direction direction) {
        this.flower = flower;
        this.direction = direction;
    }

    @Override
    public void execute() {
        Coordinate nextCoordinate = flower.getCoordinate().getNextCoordinate(direction);
        while (!game.getEntityManager().isNotInsideField(nextCoordinate)) {
            Entity entity = game.getEntityManager().getObject(nextCoordinate);
            Terrain terrain = game.getTerrainManager().getObject(nextCoordinate);
            if (terrain.getElevation() != Terrain.ELEVATION_GRASS
                    || entity != null
                    || (terrain instanceof FlowerFinish
                    && ((FlowerFinish) terrain).getOpposite() == flower.getEntityType()))
                break;
            game.getEntityManager().setObject(nextCoordinate, TileMapBuilder.build(flower.getEntityType(), nextCoordinate));
            nextCoordinate = nextCoordinate.getNextCoordinate(direction);
        }
    }
}
