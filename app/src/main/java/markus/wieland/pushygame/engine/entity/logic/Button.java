package markus.wieland.pushygame.engine.entity.logic;

import java.util.Objects;

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

        ButtonThread thread = new ButtonThread(game);
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
    public boolean isOutput(Direction direction) {
        return true;
    }

    private class ButtonThread extends Thread {

        private static final int BUTTON_ACTIVE_DURATION_MS = 3 * LogicGate.TICK + 50;

        private final Game game;

        public ButtonThread(Game game) {
            this.game = game;
        }

        @Override
        public void run() {
            try {
                for (Direction direction : Objects.requireNonNull(Direction.class.getEnumConstants())) {
                    game.execute(new LogicEvent(getCoordinate().getNextCoordinate(direction), direction));
                }

                game.getActivity().runOnUiThread(() -> game.getEntityManager().invalidate(Button.this));

                Thread.sleep(BUTTON_ACTIVE_DURATION_MS);
                active = false;
                for (Direction direction1 : Objects.requireNonNull(Direction.class.getEnumConstants())) {
                    game.execute(new LogicEvent(getCoordinate().getNextCoordinate(direction1), direction1));
                }
                game.getActivity().runOnUiThread(() -> game.getEntityManager().invalidate(Button.this));
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }

    }

}
