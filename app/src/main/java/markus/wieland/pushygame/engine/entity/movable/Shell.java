package markus.wieland.pushygame.engine.entity.movable;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.collectible.Pearl;
import markus.wieland.pushygame.engine.helper.Coordinate;

public class Shell extends MovableEntity {

    private boolean isOpen;
    private boolean hasPearl;

    public Shell(Coordinate coordinate) {
        super(coordinate, R.drawable.shell);
        isOpen = false;
        hasPearl = true;
    }

    public Shell(Coordinate coordinate, boolean isOpen, boolean hasPearl) {
        super(coordinate, R.drawable.shell);
        this.isOpen = isOpen;
        this.hasPearl = hasPearl;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public boolean hasPearl() {
        return hasPearl;
    }

    public void setHasPearl(boolean hasPearl) {
        this.hasPearl = hasPearl;
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
            game.getInventory().add(new Pearl(getCoordinate()));
            hasPearl = false;
            game.getEntityManager().invalidate(this);
        }
        return false;
    }

}
