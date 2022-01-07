package markus.wieland.pushygame.engine.events;


import markus.wieland.pushygame.engine.Game;

public abstract class Event {

    protected Game game;

    public Event() {
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public abstract void execute();

}
