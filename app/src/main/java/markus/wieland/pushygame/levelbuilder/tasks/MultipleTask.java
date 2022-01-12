package markus.wieland.pushygame.levelbuilder.tasks;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.levelbuilder.LevelBuilder;

public abstract class MultipleTask extends Task {

    private final List<Task> executed;

    public MultipleTask(LevelBuilder levelBuilder) {
        super(levelBuilder);
        executed = new ArrayList<>();
    }

    public void executeSubTask(Task task) {
        task.execute();
        executed.add(task);
    }


    @Override
    public void undo() {
        for (Task task : executed) {
            task.undo();
        }
    }
}
