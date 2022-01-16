package markus.wieland.pushygame.engine.entity.logic;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.terrain.Cable;
import markus.wieland.pushygame.engine.terrain.Terrain;

public interface LogicInput {

    boolean isInputActive(Game game, Direction direction);

    boolean isInput(Direction direction);

    PortType getPortType(Direction direction);

    void update(Game game);

    static boolean isInputActive(Game game, Direction direction, Coordinate currentCoordinate) {
        Coordinate coordinate = currentCoordinate.getNextCoordinate(direction);

        if (game.getEntityManager().isNotInsideField(coordinate)) return false;

        Entity entity = game.getEntityManager().getObject(coordinate);
        Terrain terrain = game.getTerrainManager().getObject(coordinate);

        if (entity instanceof LogicOutput && ((LogicOutput) entity).isOutput(direction.getOppositeDirection())) {
            return ((LogicOutput) entity).isOutputActive(game);
        }

        if (terrain instanceof LogicOutput && ((LogicOutput) terrain).isOutput(direction.getOppositeDirection())) {
            return ((LogicOutput) terrain).isOutputActive(game);
        }

        if (!(terrain instanceof Cable)) return false;
        return ((Cable) terrain).isActive();
    }

    static int drawableByDirection(Direction direction){
        direction = direction.getOppositeDirection();
        switch (direction) {
            case NORTH:
                return R.drawable.input_north;
            case WEST:
                return R.drawable.input_west;
            case EAST:
                return R.drawable.input_east;
            default:
                return R.drawable.input_south;
        }
    }
}
