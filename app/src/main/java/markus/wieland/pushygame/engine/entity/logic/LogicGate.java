package markus.wieland.pushygame.engine.entity.logic;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.events.LogicEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;
import markus.wieland.pushygame.engine.terrain.Cable;
import markus.wieland.pushygame.engine.terrain.Terrain;

public abstract class LogicGate extends Entity implements LogicOutput {

    private final Ports ports;

    private boolean currentOutput;

    public PortType getPortType(Direction direction) {
        return ports.getPortType(direction);
    }

    public LogicGate(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
        this.ports = new Ports();
        for (Direction direction : Direction.class.getEnumConstants()) {
            ports.configure(direction, configurePortType(direction));
        }
        this.ports.updateLists();
    }

    public Ports getPorts() {
        return ports;
    }

    public PortType configurePortNorth() {
        return PortType.VOID;
    }

    public PortType configurePortSouth() {
        return PortType.VOID;
    }

    public PortType configurePortEast() {
        return PortType.VOID;
    }

    public PortType configurePortWest() {
        return PortType.VOID;
    }

    public PortType configurePortType(Direction direction) {
        switch (direction) {
            case NORTH:
                return configurePortNorth();
            case WEST:
                return configurePortWest();
            case EAST:
                return configurePortEast();
            default:
                return configurePortSouth();
        }
    }

    public boolean isInputActive(Game game, Direction direction) {
        if (ports.getPortType(direction) != PortType.INPUT) throw new IllegalArgumentException();
        Coordinate coordinate = getCoordinate().getNextCoordinate(direction);

        if (game.getEntityManager().isNotInsideField(coordinate)) return false;
        Entity entity = game.getEntityManager().getObject(coordinate);
        if (entity instanceof LogicOutput) {
            return ((LogicOutput) entity).isOutputActive(game);
        }
        Terrain terrain = game.getTerrainManager().getObject(coordinate);
        if (!(terrain instanceof Cable)) return false;
        return ((Cable) terrain).isActive();
    }

    public void update(Game game) {
        boolean isOutputActive = isOutputActive(game);
        if (currentOutput == isOutputActive) return;
        currentOutput = isOutputActive;
        game.getEntityManager().invalidate(this);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(300);
                ((Activity) game.getEntityManager().getView(getCoordinate()).getContext()).runOnUiThread(() -> {
                    for (Direction direction : getPorts().getOutputs()) {
                        game.execute(new LogicEvent(getCoordinate().getNextCoordinate(direction), direction));
                    }

                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

}
