package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.logic.Edge;
import markus.wieland.pushygame.engine.entity.logic.LogicInput;
import markus.wieland.pushygame.engine.entity.logic.LogicOutput;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.helper.Field;
import markus.wieland.pushygame.engine.terrain.Cable;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class LogicEvent extends Event {

    private final Edge edge;
    private final Direction direction;
    private final Coordinate coordinate;

    public LogicEvent(Coordinate coordinate, Direction direction) {
        this.edge = new Edge();
        this.coordinate = coordinate;
        this.direction = direction;
    }

    @Override
    public void execute() {
        Entity entity = getGame().getEntityManager().getObject(coordinate);
        Terrain terrain = getGame().getTerrainManager().getObject(coordinate);
        if (entity instanceof LogicInput && ((LogicInput) entity).isInput(direction)) {
            edge.addInput((LogicInput) entity);
        }
        if (terrain instanceof LogicInput && ((LogicInput) terrain).isInput(direction)) {
            edge.addInput((LogicInput) terrain);
        }
        if (terrain instanceof Cable) {
            getEdgeFrom((Cable) terrain);
        }

        edge.update(getGame());
    }

    private void getEdgeFrom(Cable cable) {
        check(Direction.NORTH, cable.getCoordinate());
        check(Direction.SOUTH, cable.getCoordinate());
        check(Direction.WEST, cable.getCoordinate());
        check(Direction.EAST, cable.getCoordinate());
    }

    private void check(Direction direction, Coordinate coordinate) {
        Entity entity = game.getEntityManager().getObject(coordinate);
        Terrain terrain = game.getTerrainManager().getObject(coordinate);
        if (isOutput(entity, direction)) {
            edge.addOutput((LogicOutput) entity);
            return;
        }
        if (isInput(entity, direction)) {
            edge.addInput((LogicInput) entity);
            return;
        }
        if (isOutput(terrain, direction)) {
            edge.addOutput((LogicOutput) terrain);
            return;
        }
        if (isInput(terrain, direction)) {
            edge.addInput((LogicInput) terrain);
            return;
        }

        if (!(terrain instanceof Cable)) return;

        Cable cable = (Cable) terrain;
        if (edge.contains(cable)) return;

        edge.add(cable);

        check(Direction.NORTH, coordinate.getNextCoordinate(Direction.NORTH));
        check(Direction.SOUTH, coordinate.getNextCoordinate(Direction.SOUTH));
        check(Direction.EAST, coordinate.getNextCoordinate(Direction.EAST));
        check(Direction.WEST, coordinate.getNextCoordinate(Direction.WEST));

    }

    private boolean isOutput(Field field, Direction direction) {
        return field instanceof LogicOutput
                && ((LogicOutput) field).isOutput(direction)
                && !edge.containsOutput((LogicOutput) field);
    }

    private boolean isInput(Field field, Direction direction) {
        return field instanceof LogicInput
                && ((LogicInput) field).isInput(direction)
                && !edge.containsInput((LogicInput) field);
    }
}
