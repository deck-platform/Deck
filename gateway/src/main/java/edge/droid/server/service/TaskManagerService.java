package edge.droid.server.service;

import edge.droid.server.model.Task;

import java.util.Optional;

public interface TaskManagerService {

    boolean tranTask(Task task, String FLModelPath, int times);

    Task getTaskByID(String taskID);
}
