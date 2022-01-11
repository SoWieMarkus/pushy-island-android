package markus.wieland.pushygame.engine.entity.statics;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.entity.Entity;
import markus.wieland.pushygame.engine.exceptions.StringDirectionException;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class String extends Entity {

    private int drawable;

    public String(Coordinate coordinate, Direction first, Direction second) {
        super(coordinate, null);
        if (first == null) {
            switch (second) {
                case NORTH:
                    setDrawable(R.drawable.string_half_north);
                    return;
                case EAST:
                    setDrawable(R.drawable.string_half_east);
                    return;
                case SOUTH:
                    setDrawable(R.drawable.string_half_south);
                    return;
                case WEST:
                    setDrawable(R.drawable.string_half_west);
                    return;
                default:
                    throw new StringDirectionException(first, second);
            }
        }

        switch (first) {
            case EAST:
                switch (second) {
                    case NORTH:
                        setDrawable(R.drawable.string_west_north);
                        break;
                    case SOUTH:
                        setDrawable(R.drawable.string_south_west);
                        break;
                    case EAST:
                        setDrawable(R.drawable.string_east_west);
                        break;
                    default:
                        throw new StringDirectionException(first,second);
                }
                break;
            case WEST:
                switch (second) {
                    case NORTH:
                        setDrawable(R.drawable.string_north_east);
                        break;
                    case WEST:
                        setDrawable(R.drawable.string_east_west);
                        break;
                    case SOUTH:
                        setDrawable(R.drawable.string_east_south);
                        break;
                    default:
                        throw new StringDirectionException(first,second);
                }
                break;
            case NORTH:
                switch (second) {
                    case EAST:
                        setDrawable(R.drawable.string_east_south);
                        break;
                    case NORTH:
                        setDrawable(R.drawable.string_north_south);
                        break;
                    case WEST:
                        setDrawable(R.drawable.string_south_west);
                        break;
                    default:
                        throw new StringDirectionException(first,second);
                }
                break;
            case SOUTH:
                switch (second) {
                    case SOUTH:
                        setDrawable(R.drawable.string_north_south);
                        break;
                    case EAST:
                        setDrawable(R.drawable.string_north_east);
                        break;
                    case WEST:
                        setDrawable(R.drawable.string_west_north);
                        break;
                    default:
                        throw new StringDirectionException(first,second);
                }
                break;
        }
    }

    @Override
    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    // Can not be placed by user
    @Override
    public EntityType getType() {
        return null;
    }
}
