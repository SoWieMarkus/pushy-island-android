package markus.wieland.pushygame.engine.entity.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import markus.wieland.pushygame.engine.helper.Direction;

public class Ports {

    private final HashMap<Direction, PortType> portsDirection;
    private final List<Direction> inputs;
    private final List<Direction> outputs;

    public Ports() {
        this.portsDirection = new HashMap<>();
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.portsDirection.put(Direction.NORTH, PortType.VOID);
        this.portsDirection.put(Direction.SOUTH, PortType.VOID);
        this.portsDirection.put(Direction.EAST, PortType.VOID);
        this.portsDirection.put(Direction.WEST, PortType.VOID);
    }

    public void configure(Direction direction, PortType portType) {
        this.portsDirection.put(direction, portType);
    }

    public void updateLists(){
        this.outputs.clear();
        this.inputs.clear();

        for (Map.Entry<Direction, PortType> entry : portsDirection.entrySet()) {
            if (entry.getValue() == PortType.INPUT) inputs.add(entry.getKey());
            if (entry.getValue() == PortType.OUTPUT) outputs.add(entry.getKey());
        }
    }

    public List<Direction> getInputs() {
        return inputs;
    }

    public List<Direction> getOutputs() {
        return outputs;
    }

    public PortType getPortType(Direction direction) {
        return this.portsDirection.get(direction);
    }
}
