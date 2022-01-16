package markus.wieland.pushygame.engine.entity.logic;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class NotGate extends LogicGate {

    public NotGate(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    public PortType configurePortNorth() {
        return PortType.OUTPUT;
    }

    @Override
    public PortType configurePortSouth() {
        return PortType.INPUT;
    }

    @Override
    public boolean isOutputActive(Game game) {
        boolean output = false;
        for (Direction direction : getPorts().getInputs()) {
            output = isInputActive(game, direction) || output;
        }
        return !output;
    }
}
