package markus.wieland.pushygame.engine.entity.logic;

import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.helper.Direction;

public interface LogicOutput {

    boolean isOutputActive(Game game);

    boolean isOutput(Direction direction);

    PortType getPortType(Direction direction);

}
