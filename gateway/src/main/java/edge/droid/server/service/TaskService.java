package edge.droid.server.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public interface TaskService {

    Map<Object, Object> getTaskTimeLog(String taskID);

    void createTaskTimeLog(String taskID, String logDataMap);

    String createTask(MultipartFile[] fileList, String description, MultipartFile[] libFileList, int limitModelNum, String taskType, int trainNum) throws IOException;

    String runTask(String taskID);

    String getDLModel(String taskID, Integer waitSeconds) throws InterruptedException;

    List<String> getTask(String taskID, Integer limit, Integer waitSeconds) throws InterruptedException;

    List<String> getFLResult(String taskID, Integer waitSeconds);

    Map<String, Object> aggregateResult(String taskID, String operationType, String operationColumn, List<String> aggregateColumnList, Integer limit, Integer waitSeconds, String otherParam) throws InterruptedException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException;
}
