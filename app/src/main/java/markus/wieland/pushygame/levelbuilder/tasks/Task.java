package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public abstract class Task {

    private final LevelBuilder levelBuilder;

    public Task(LevelBuilder levelBuilder) {
        this.levelBuilder = levelBuilder;
    }

    public abstract void execute();

    public abstract void undo();

    public LevelBuilder getLevelBuilder() {
        return levelBuilder;
    }
}
