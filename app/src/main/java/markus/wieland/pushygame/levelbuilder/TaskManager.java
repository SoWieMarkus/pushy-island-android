package markus.wieland.pushygame.levelbuilder;

import java.util.ArrayList;
import java.util.List;

import markus.wieland.pushygame.levelbuilder.tasks.Task;

public class TaskManager {

    private final List<Task> tasksToUndo;
    private final List<Task> tasksToRedo;

    private TaskManager(){
        this.tasksToRedo = new ArrayList<>();
        this.tasksToUndo = new ArrayList<>();
    }

    private static final TaskManager instance = new TaskManager();

    public static TaskManager getInstance() {
        return instance;
    }

    public void execute(Task task) {
        task.execute();
        tasksToUndo.add(task);
    }

    public void undo(){
        if (tasksToUndo.isEmpty()) return;
        Task task = tasksToUndo.get(tasksToUndo.size() - 1);
        task.undo();
        tasksToUndo.remove(task);
        tasksToRedo.add(task);
    }

    public void redo(){
        if (tasksToRedo.isEmpty()) return;
        Task task = tasksToRedo.get(tasksToRedo.size() - 1);
        task.execute();
        tasksToRedo.remove(task);
        tasksToUndo.add(task);
    }
}
