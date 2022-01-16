package markus.wieland.pushygame.engine.entity.logic;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.movable.MovableEntity;
import markus.wieland.pushygame.engine.events.LogicEvent;
import markus.wieland.pushygame.engine.events.UpdateCableEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class PowerBlock extends MovableEntity implements LogicOutput {
    public PowerBlock(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    public boolean isOutputActive(Game game) {
        return true;
    }

    @Override
    protected void executeMove(Coordinate nextCoordinate, Game game) {
        Coordinate coordinateBefore = new Coordinate(getCoordinate().getX(), getCoordinate().getY());
        super.executeMove(nextCoordinate, game);
        executePowerEvent(game, coordinateBefore, nextCoordinate);
    }

    public void executePowerEvent(Game game, Coordinate coordinateBefore, Coordinate nextCoordinate) {
        for (Direction direction : Direction.class.getEnumConstants()) {
            game.execute(new LogicEvent(coordinateBefore.getNextCoordinate(direction), direction));
            if (nextCoordinate == null) continue;
            game.execute(new LogicEvent(nextCoordinate.getNextCoordinate(direction), direction));
        }
        game.execute(new UpdateCableEvent());
    }

    @Override
    public PortType getPortType(Direction direction) {
        return PortType.OUTPUT;
    }
}
