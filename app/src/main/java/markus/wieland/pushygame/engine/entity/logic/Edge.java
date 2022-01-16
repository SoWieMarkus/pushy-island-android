package markus.wieland.pushygame.engine.entity.logic;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.terrain.Cable;

public class Edge {

    private boolean active;

    private final List<Cable> cables;

    private final List<LogicOutput> outputs;
    private final List<LogicInput> inputs;

    public Edge() {
        this.active = false;
        this.cables = new ArrayList<>();
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
    }

    public void add(Cable cable) {
        this.cables.add(cable);
    }

    public void addInput(LogicInput input) {
        this.inputs.add(input);
    }

    public void addOutput(LogicOutput output) {
        this.outputs.add(output);
    }

    public boolean isActive() {
        return active;
    }

    public List<Cable> getCables() {
        return cables;
    }

    public void update(Game game) {
        active = false;


        for (LogicOutput logicOutput : outputs) {
            active = active || logicOutput.isOutputActive(game);
        }

        for (Cable cable : cables) {
            cable.setActive(active);
            game.getTerrainManager().invalidate(cable);
        }

        for (LogicInput logicInput : inputs) {
            logicInput.update(game);
        }

    }

    public boolean contains(Cable cable) {
        return cables.contains(cable);
    }

    public boolean containsInput(LogicInput logicGate) {
        return inputs.contains(logicGate);
    }

    public boolean containsOutput(LogicOutput logicOutput) {
        return outputs.contains(logicOutput);
    }
}
