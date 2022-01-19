package markus.wieland.pushygame.engine.terrain.pressure;

import java.util.Objects;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.entity.logic.LogicOutput;
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
    public void interact(Entity entity, Game game) {
        if (entity == lastEntity) return;
        lastEntity = entity;
        for (Direction direction : Objects.requireNonNull(Direction.class.getEnumConstants())) {
            game.execute(new LogicEvent(getCoordinate().getNextCoordinate(direction), direction));
        }
    }
}
