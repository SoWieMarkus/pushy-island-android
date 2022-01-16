package markus.wieland.pushygame.engine.terrain;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.logic.LogicInput;
import markus.wieland.pushygame.engine.entity.logic.PortType;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.TerrainType;

public class Door extends Sand implements LogicInput {

    private boolean open;

    public Door(Coordinate coordinate, TerrainType terrainType) {
        super(coordinate, terrainType);
    }

    @Override
    public int getDrawable() {
        return open ? R.drawable.sand : super.getDrawable();
    }

    @Override
    public boolean isInputActive(Game game, Direction direction) {
        return LogicInput.isInputActive(game, direction, getCoordinate());
    }

    @Override
    public boolean isInput(Direction direction) {
        return true;
    }

    @Override
    public PortType getPortType(Direction direction) {
        return PortType.INPUT;
    }

    @Override
    public int getElevation() {
        return open ? super.getElevation() : Integer.MAX_VALUE;
    }

    @Override
    public void update(Game game) {
        open = false;
        for (Direction direction : Direction.class.getEnumConstants()) {
            if (isInputActive(game, direction)) {
                open = true;
            }
        }
        game.getTerrainManager().invalidate(this);

    }
}
