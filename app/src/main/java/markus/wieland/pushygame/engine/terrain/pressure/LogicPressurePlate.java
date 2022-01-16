package markus.wieland.pushygame.engine.terrain.pressure;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.logic.LogicOutput;
import markus.wieland.pushygame.engine.entity.logic.PortType;
import markus.wieland.pushygame.engine.events.LogicEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.TerrainType;

public class LogicPressurePlate extends PressurePlateTerrain implements LogicOutput {

    private Entity lastEntity;

    public LogicPressurePlate(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    @Override
    public boolean isOutputActive(Game game) {
        return game.getEntityManager().getObject(this) != null;
    }

    @Override
    public boolean isOutput(Direction direction) {
        return true;
    }

    @Override
    public PortType getPortType(Direction direction) {
        return PortType.OUTPUT;
    }

    @Override
    public void interact(Entity entity, Game game) {
        if (entity == null || entity == lastEntity) return;
        lastEntity = entity;
        for (Direction direction : Direction.class.getEnumConstants()) {
            game.execute(new LogicEvent(getCoordinate().getNextCoordinate(direction), direction));
        }
    }
}
