package markus.wieland.pushygame.engine.entity.logic;

import android.app.Activity;

import markus.wieland.pushygame.R;
import markus.wieland.pushygame.engine.Game;
import markus.wieland.pushygame.engine.entity.InteractableEntity;
import markus.wieland.pushygame.engine.entity.collectible.Energy;
import markus.wieland.pushygame.engine.events.LogicEvent;
import markus.wieland.pushygame.engine.helper.Coordinate;
import markus.wieland.pushygame.engine.helper.Direction;
import markus.wieland.pushygame.engine.level.EntityType;

public class Button extends InteractableEntity implements LogicOutput {

    private boolean active;

    public Button(Coordinate coordinate, EntityType entityType) {
        super(coordinate, entityType);
    }

    @Override
    public int getDrawable() {
        return active ? R.drawable.button_pressed : super.getDrawable();
    }

    @Override
    protected void executeInteraction(Direction direction, Game game) {
        if (active) return;
        if (!game.getInventory().get(Energy.class, 1)) return;

        this.active = true;
        game.getEntityManager().invalidate(this);

        Thread thread = new Thread(() -> {
            for (Direction direction1 : Direction.class.getEnumConstants()) {
                game.execute(new LogicEvent(getCoordinate().getNextCoordinate(direction1), direction1));
            }

            ((Activity)game.getEntityManager().getView(getCoordinate()).getContext()).runOnUiThread(() -> game.getEntityManager().invalidate(Button.this));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            active = false;
            for (Direction direction1 : Direction.class.getEnumConstants()) {
                game.execute(new LogicEvent(getCoordinate().getNextCoordinate(direction1), direction1));
            }
            ((Activity)game.getEntityManager().getView(getCoordinate()).getContext()).runOnUiThread(() -> game.getEntityManager().invalidate(Button.this));

        });
        thread.start();




    }

    @Override
    public boolean canInteractFromThisDirection(Direction direction) {
        return true;
    }

    @Override
    public boolean isOutputActive(Game game) {
        return active;
    }

    @Override
    public void addOutgoingEdge(Edge edge) {

    }

    @Override
    public PortType getPortType(Direction direction) {
        return PortType.OUTPUT;
    }

}
