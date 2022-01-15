package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.logic.Edge;
import markus.wieland.pushygame.engine.entity.logic.LogicGate;
import markus.wieland.pushygame.engine.entity.logic.LogicOutput;
import markus.wieland.pushygame.engine.entity.logic.PortType;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
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
        if (entity instanceof LogicGate && ((LogicGate) entity).getPortType(direction.getOppositeDirection()) == PortType.INPUT) {
            edge.addInput((LogicGate) entity);
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
        if (entity instanceof LogicOutput
                && ((LogicOutput) entity).getPortType(direction.getOppositeDirection()) == PortType.OUTPUT
                && !edge.containsOutput((LogicOutput) entity)) {
            edge.addOutput((LogicOutput) entity);
            return;
        }
        if (entity instanceof LogicGate
                && ((LogicGate) entity).getPortType(direction.getOppositeDirection()) == PortType.INPUT
                && !edge.containsInput((LogicGate)entity)) {
            edge.addInput((LogicGate) entity);
            return;
        }

        Terrain terrain = game.getTerrainManager().getObject(coordinate);
        if (!(terrain instanceof Cable)) return;

        Cable cable = (Cable) terrain;
        if (edge.contains(cable)) return;

        edge.add(cable);

        check(Direction.NORTH, coordinate.getNextCoordinate(Direction.NORTH));
        check(Direction.SOUTH, coordinate.getNextCoordinate(Direction.SOUTH));
        check(Direction.EAST, coordinate.getNextCoordinate(Direction.EAST));
        check(Direction.WEST, coordinate.getNextCoordinate(Direction.WEST));

    }
}
