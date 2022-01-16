package markus.wieland.pushygame.engine.entity.statics;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class String extends Entity {

    private final int[] drawables;

    public String(Coordinate coordinate, Direction first, Direction second) {
        super(coordinate, null);
        drawables = new int[]{getDrawableFromDirection(first), getDrawableFromDirection(second.getOppositeDirection())};
    }

    public int getDrawableFromDirection(Direction direction) {
        if (direction == null) return R.drawable.no_entity;
        switch (direction) {
            case EAST:
                return R.drawable.string_half_west;
            case WEST:
                return R.drawable.string_half_east;
            case NORTH:
                return R.drawable.string_half_south;
            default:
                return R.drawable.string_half_north;
        }
    }

    @Override
    public int[] getDrawableList() {
        return drawables;
    }

    // Can not be placed by user
    @Override
    public EntityType getType() {
        return null;
    }
}
