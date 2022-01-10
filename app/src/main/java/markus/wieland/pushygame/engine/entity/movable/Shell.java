package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.collectible.Pearl;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.level.EntityType;

public class Shell extends MovableEntity {

    private boolean isOpen;
    private boolean hasPearl;

    public Shell(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
        isOpen = entityType == EntityType.SHELL_OPEN_WITH_PEARL;
        hasPearl = true;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    @Override
    public boolean isMovePossible(Coordinate nextCoordinate, Game game) {
        return super.isMovePossible(nextCoordinate, game) && !isOpen;
    }

    @Override
    public int getDrawable() {
        if (isOpen) return hasPearl ? R.drawable.shell_open_with_pearl : R.drawable.shell_open;
        return super.getDrawable();
    }

    @Override
    public boolean move(Coordinate nextCoordinate, Game game) {
        if (super.move(nextCoordinate, game)) return true;
        if (isOpen && hasPearl) {
            game.getInventory().add(new Pearl(getCoordinate(), EntityType.PEARL));
            hasPearl = false;
            game.getEntityManager().invalidate(this);
        }
        return false;
    }

    @Override
    public EntityType getType() {
        return EntityType.SHELL;
    }
}
