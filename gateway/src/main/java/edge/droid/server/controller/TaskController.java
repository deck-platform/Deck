package edge.droid.server.controller;

import edge.droid.server.service.TaskService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@Api
public class TaskController {

    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "/task/timelog/get", method = RequestMethod.GET)
    public Map<Object, Object> getTaskTimeLog(@RequestParam("task_id") String taskID) {
        log.info("[getTaskTimeLog] taskID={}", taskID);
        return taskService.getTaskTimeLog(taskID);
    }

    @RequestMapping(value = "/task/timelog/create", method = RequestMethod.POST)
    public void createTaskTimeLog(@RequestParam("task_id") String taskID,
                                    @RequestParam("log_data_map") String logDataMap) {
        log.info("[createTaskTimeLog] taskID={}", taskID);
        taskService.createTaskTimeLog(taskID, logDataMap);
    }

    @RequestMapping(value = "/task/run", method = RequestMethod.POST)
    public String runTask(@RequestParam("task_id") String taskID) {
        log.info("[runTask] taskID={}", taskID);
        return taskService.runTask(taskID);
    }

    @RequestMapping(value = "/task/create", method = RequestMethod.POST)
    public String createTask(@RequestParam("file") MultipartFile[] fileList,
                             @RequestParam(value = "lib_file", required = false) MultipartFile[] libFileList,
                             @RequestParam(name = "limit_model_num", required = false, defaultValue = "0") int limitModelNum,
                             @RequestParam(name = "task_type", required = false, defaultValue = "default") String taskType,
                             @RequestParam(name = "train_num", required = false, defaultValue = "0") int trainNum,
                             @RequestParam("description") String description) throws IOException {
        // todo (next) add params
        log.info("[createTask] description={}, taskType={}, limitModelNum={}, train_num={}", description, taskType, limitModelNum, trainNum);
        return taskService.createTask(fileList, description, libFileList, limitModelNum, taskType, trainNum);
    }

    @RequestMapping(value = "/task/fl/get", method = RequestMethod.GET)
    public String getFLModel(@RequestParam("task_id") String taskID,
                                      @RequestParam(name = "wait_seconds", required = false, defaultValue = "0") Integer waitSeconds) throws InterruptedException {
        // todo (next) add params
        log.info("[getFLModel], taskID={}, waitSeconds={}", taskID, waitSeconds);
        return taskService.getDLModel(taskID, waitSeconds);
    }

    @RequestMapping(value = "/task/fl/train/trace/get", method = RequestMethod.GET)
    public List<String> getFLTrainTrace(@RequestParam("task_id") String taskID,
                              @RequestParam(name = "wait_seconds", required = false, defaultValue = "0") Integer waitSeconds) throws InterruptedException {
        log.info("[getFLTrainTrace], taskID={}", taskID);
        return taskService.getFLResult(taskID, waitSeconds);
    }

    @RequestMapping(value = "/task/get", method = RequestMethod.GET)
    public List<String> getTaskResult(@RequestParam("task_id") String taskID,
                                      @RequestParam(name = "wait_seconds", required = false, defaultValue = "0") Integer waitSeconds,
                                      @RequestParam(name = "limit", required = false) Integer limit) throws InterruptedException {
        // todo (next) add params
        log.info("[getTaskResult], taskID={} limit={}", taskID, limit);
        return taskService.getTask(taskID, limit, waitSeconds);
    }

    @RequestMapping(value = "/task/aggregate", method = RequestMethod.GET)
    public Map<String, Object> aggregateResult(@RequestParam("task_id") String taskID,
                                               @RequestParam("aggregate_column") List<String> aggregateColumnList,
                                               @RequestParam("operation_type") String operationType,
                                               @RequestParam("operation_column") String operationColumn,
                                               @RequestParam(name = "other_param", required = false, defaultValue = "") String otherParam,
                                               @RequestParam(name = "wait_seconds", required = false, defaultValue = "0") Integer waitSeconds,
                                               @RequestParam(name = "limit", required = false) Integer limit) throws InterruptedException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        log.info("[aggregateResult], taskID={} operation={}, limit={}, waitSeconds={}, aggregateColumnList={}, operationColumn={}, otherParam={}", taskID, operationType, limit, waitSeconds, aggregateColumnList, operationColumn, otherParam);
        return taskService.aggregateResult(taskID, operationType, operationColumn, aggregateColumnList, limit, waitSeconds, otherParam);
    }
}

