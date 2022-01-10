package markus.wieland.pushygame.engine.entity.interactable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.InteractableEntity;
import markus.wieland.pushygame.engine.events.ChangeLeafDirectionEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class LeafDirectionChanger extends InteractableEntity {

    private Direction direction;

    public LeafDirectionChanger(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
        switch (entityType) {
            case LEAF_CHANGER_EAST:
                direction = Direction.EAST;
                break;
            case LEAF_CHANGER_WEST:
                direction = Direction.WEST;
                break;
            case LEAF_CHANGER_SOUTH:
                direction = Direction.SOUTH;
                break;
            default:
                direction = Direction.NORTH;
                break;
        }
    }

    @Override
    public int getDrawable() {
        switch (direction) {
            case EAST:
                return R.drawable.leaf_changer_east;
            case WEST:
                return R.drawable.leaf_changer_west;
            case NORTH:
                return R.drawable.leaf_changer_north;
            default:
                return R.drawable.leaf_changer_south;
        }
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        this.direction = direction;
        game.execute(new ChangeLeafDirectionEvent(direction));
        game.getEntityManager().invalidate(this);
    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return true;
    }

    @Override
    public EntityType getType() {
        switch (direction) {
            case EAST:
                return EntityType.LEAF_CHANGER_EAST;
            case WEST:
                return EntityType.LEAF_CHANGER_WEST;
            case NORTH:
                return EntityType.LEAF_CHANGER_NORTH;
            default:
                return EntityType.LEAF_CHANGER_SOUTH;
        }
    }
}
