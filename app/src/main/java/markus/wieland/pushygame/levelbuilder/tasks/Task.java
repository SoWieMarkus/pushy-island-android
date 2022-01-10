package markus.wieland.pushygame.levelbuilder.tasks;

import markus.wieland.pushygame.engine.helper.Field;
import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public abstract class Task {

    private final LevelBuilder levelBuilder;

    public Task(LevelBuilder levelBuilder) {
        this.levelBuilder = levelBuilder;
    }

    public abstract <F extends Field> void execute();

    public abstract <F extends Field> void undo();

    public LevelBuilder getLevelBuilder() {
        return levelBuilder;
    }
}
