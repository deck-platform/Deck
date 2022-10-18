package edge.droid.server.wrapper;

import edge.droid.server.data.Source;
import edge.droid.server.model.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TaskWrapper {

    public Task task(String fileDir, String description, String dexFilePath, String taskID, String libPath, int limitModelNum, String taskType, int trainNum, Map<Source, List<String>> permissionInfo) {
        Task task = new Task();
        task.setTrainNum(trainNum);
        task.setFileDir(fileDir);
        task.setDescription(description);
        task.setDexPath(dexFilePath);
        task.setTaskID(taskID);
        task.setLibPath(libPath);
        task.setLimitModelNum(limitModelNum);
        task.setTaskType(taskType);
        task.setPermissionInfo(permissionInfo);
        return task;
    }
}
