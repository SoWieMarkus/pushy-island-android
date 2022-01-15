package markus.wieland.pushygame.engine;

import java.util.List;

import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.logic.Edge;
import markus.wieland.pushygame.engine.entity.logic.LogicGate;
import markus.wieland.pushygame.engine.entity.logic.LogicOutput;
import markus.wieland.pushygame.engine.entity.logic.PortType;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.terrain.Cable;
import markus.wieland.pushygame.engine.terrain.Terrain;

public class LogicInitializer {

    private EntityManager entityManager;
    private TerrainManager terrainManager;
    private List<Cable> cables;
    private Edge currentEdge;

    public LogicInitializer(EntityManager entityManager, TerrainManager terrainManager) {
        this.entityManager = entityManager;
        this.terrainManager = terrainManager;
        this.cables = terrainManager.getOfType(Cable.class);
        if (this.cables.isEmpty()) return;
        initialize();
    }

    private void initialize(){
        while (!cables.isEmpty()) {
            currentEdge = new Edge();
            getEdgeFrom(cables.get(0));
        }


        // TODO Pressure plates
        for (LogicGate logicGate : entityManager.getOfType(LogicGate.class)) {
            for (Direction direction : logicGate.getPorts().getOutputs()) {
                checkDirectConnection(logicGate, direction);
            }
        }
    }

    private void checkDirectConnection(LogicGate logicGate, Direction direction){
        Coordinate coordinate = logicGate.getCoordinate().getNextCoordinate(direction);
        if (entityManager.isNotInsideField(coordinate)) return;
        Entity entity = entityManager.getObject(coordinate);
        if (entity instanceof LogicGate && ((LogicGate) entity).getPortType(direction.getOppositeDirection()) == PortType.INPUT ) {
            Edge edge = new Edge();
            edge.addInput((LogicGate)entity);
            edge.addOutput((LogicGate)entity);
            logicGate.addOutgoingEdge(edge);
        }
    }

    private void getEdgeFrom(Cable cable) {
        check(Direction.NORTH, cable.getCoordinate());
        check(Direction.SOUTH, cable.getCoordinate());
        check(Direction.WEST, cable.getCoordinate());
        check(Direction.EAST, cable.getCoordinate());
    }

    private void check(Direction direction, Coordinate coordinate) {
        Entity entity = entityManager.getObject(coordinate);
        if (entity instanceof LogicOutput && ((LogicOutput) entity).getPortType(direction.getOppositeDirection()) == PortType.OUTPUT) {
            ((LogicOutput) entity).addOutgoingEdge(currentEdge);
            currentEdge.addOutput((LogicOutput) entity);
            return;
        }
        if (entity instanceof LogicGate && ((LogicGate) entity).getPortType(direction.getOppositeDirection()) == PortType.INPUT) {
            currentEdge.addInput((LogicGate) entity);
            return;
        }

        Terrain terrain = terrainManager.getObject(coordinate);
        if (!(terrain instanceof Cable)) return;

        Cable cable = (Cable) terrain;
        if (!cables.contains(cable)) return;
        cables.remove(cable);
        currentEdge.add(cable);

        check(Direction.NORTH, coordinate.getNextCoordinate(Direction.NORTH));
        check(Direction.SOUTH, coordinate.getNextCoordinate(Direction.SOUTH));
        check(Direction.EAST, coordinate.getNextCoordinate(Direction.EAST));
        check(Direction.WEST, coordinate.getNextCoordinate(Direction.WEST));

    }
}
