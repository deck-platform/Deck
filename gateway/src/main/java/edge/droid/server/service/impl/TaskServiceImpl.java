package edge.droid.server.service.impl;

import com.alibaba.fastjson.JSON;
import edge.droid.server.aop.TimeLogAop;
import edge.droid.server.data.AuthorityResult;
import edge.droid.server.data.GlobalData;
import edge.droid.server.data.Source;
import edge.droid.server.model.Task;
import edge.droid.server.redis.Redis;
import edge.droid.server.service.CheckerService;
import edge.droid.server.service.TaskManagerService;
import edge.droid.server.service.TaskService;
import edge.droid.server.service.TimeLogService;
import edge.droid.server.utils.DecompileUtils;
import edge.droid.server.utils.DexUtils;
import edge.droid.server.utils.FileUtils;
import edge.droid.server.utils.JavaAnalysisUtils;
import edge.droid.server.wrapper.TaskWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import joinery.DataFrame;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private Redis redis;
    @Autowired
    private TaskWrapper taskWrapper;
    @Autowired
    private TaskManagerService taskManagerService;
    @Autowired
    private TimeLogService timeLogService;
    @Autowired
    private CheckerService checkerService;

    @Value("${task.base.path}")
    private String taskDirBasePath;
    @Value("${fl.base.model}")
    private String FLBaseModelPath;
    private final int DEFAULT_TIMES = 1;

    @Override
    public Map<Object, Object> getTaskTimeLog(String taskID) {
        String key = String.format(GlobalData.timeLogKey, taskID);
        return redis.hmGetAll(key);
    }

    @Override
    public void createTaskTimeLog(String taskID, String logDataMap) {
        Map<String, Long> map =  JSON.parseObject(logDataMap, HashMap.class);
        map.forEach((key, value) -> {
            timeLogService.setTimeLog(GlobalData.USER, taskID, key, String.valueOf(value));
        });
    }

    @Override
    public String createTask(MultipartFile[] fileList, String description, MultipartFile[] libFileList, int limitModelNum, String taskType, int trainNum) throws IOException {

        checkCreateParams(limitModelNum, taskType);

        // 0. generate taskID
        String taskID = description + "-" + System.currentTimeMillis() + "-task";

        timeLogService.setTimeLog(GlobalData.SERVER, taskID, "createTaskBegin", String.valueOf(System.currentTimeMillis()));

        // 1. mkdir task dir
        String taskDirPath = taskDirBasePath + taskID + "/";
        boolean mkdirResult = FileUtils.createDirs(taskDirPath);
        if (!mkdirResult) {
            log.error("[createTask] mkdir task path={} err", taskDirPath);
            throw new RuntimeException("mkdir task dir err");
        }
        String libDirPath = taskDirPath + "lib/";
        boolean mkdirLibResult = FileUtils.createDirs(libDirPath);
        if (!mkdirLibResult) {
            log.error("[createTask] mkdir lib path={} err", libDirPath);
            throw new RuntimeException("mkdir lib dir err");
        }


        // 2. save file and generate class file
        for (MultipartFile file : fileList) {
            String filePath = taskDirPath + file.getOriginalFilename();
            try {
                FileUtils.saveFile(file, filePath);
            } catch (Exception e) {
                log.error("[createTask] save class file error", e);
                throw new RuntimeException("save class file error");
            }
            boolean decompileResult = DecompileUtils.decompile(taskDirPath, file.getOriginalFilename());
            if (!decompileResult) {
                log.error("[createTask] generate raw java error");
                throw new RuntimeException("decompile java error");
            }
        }
        if (null != libFileList) {
            for (MultipartFile file : libFileList) {
                /*
                debug code
                 */
                if (file.getOriginalFilename().contains("fastjson")) {
                    continue;
                }
                String filePath = libDirPath + file.getOriginalFilename();
                try {
                    FileUtils.saveFile(file, filePath);
                } catch (Exception e) {
                    log.error("[createTask] save lib file error");
                    throw new RuntimeException("save lib file error");
                }
            }
        }
        List<File> javaFileList = Arrays.stream(new File(taskDirPath).listFiles())
                .filter(file -> file.getPath().endsWith(".java"))
                .collect(Collectors.toList());
        Map<Source, List<String>> sourceListMap = JavaAnalysisUtils.getAnnotationMap(javaFileList);
        log.info("[createTask] sourceListMap={}", sourceListMap);


        // 3. code checker todo multiThread
//        AuthorityResult authorityResult = checkerService.checkCodeSecurity(new File(taskDirPath));
//        if (authorityResult != AuthorityResult.SUCCESS) {
//            log.error("[createTask] don't pass code check, result={}", authorityResult);
//            throw new RuntimeException(authorityResult.getDescription());
//        }

        // 4. create task
        List<String> dexFilePathList = generateDex(taskDirPath, libDirPath, taskID);
        if (dexFilePathList.size() == 0) {
            throw new RuntimeException("generate dex err");
        }
        // now only have one dex file
        // Todo test soot insert
        checkerService.sootInsert(dexFilePathList.get(0), taskDirPath);
        Task task = taskWrapper.task(taskDirPath, description, dexFilePathList.get(0), taskID, libDirPath, limitModelNum, taskType, trainNum, sourceListMap);
        redis.set(task.getTaskID(), JSON.toJSONString(task));

        timeLogService.setTimeLog(GlobalData.SERVER, taskID, "createTaskEnd", String.valueOf(System.currentTimeMillis()));

        GlobalData.taskID2Task.put(taskID, task);
        return task.getTaskID();
    }

    @Override
    @TimeLogAop(type="server", desc="runTask")
    public String runTask(String taskID) {
        Task task = taskManagerService.getTaskByID(taskID);
        boolean result = taskManagerService.tranTask(task, FLBaseModelPath, DEFAULT_TIMES);
        if (result) {
            log.info("[runTask] taskID={} tran success", taskID);
            return String.format("%s tran success", taskID);
        }
        log.error("[runTask] taskID={} tran error", taskID);
        return String.format("%s tran err", taskID);
    }

    @Override
    @TimeLogAop(type="server", desc="getDLModel")
    public String getDLModel(String taskID, Integer waitSeconds) throws InterruptedException {

        Task task = taskManagerService.getTaskByID(taskID);
        File file = new File(String.format("%s2_mnist.snapshot.mnn", taskDirBasePath + taskID + "/" + "FLFile/" + task.getTrainNum() + "/"));
        if (file.exists()) {
            return FileUtils.fileToBase64(file.getAbsolutePath());
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);
        GlobalData.taskIDFL2LatchMap.put(taskID, countDownLatch);
        boolean result = countDownLatch.await(waitSeconds, TimeUnit.SECONDS);
        if (!result) {
            log.info("[getDLModel] can not get until waitSeconds");
            throw new RuntimeException("no result");
        }
        return FileUtils.fileToBase64(file.getAbsolutePath());
    }

    @Override
    @TimeLogAop(type="server", desc="getTask")
    public List<String> getTask(String taskID, Integer limit, Integer waitSeconds) throws InterruptedException {
        List<Object> redisValueList = redis.lRange(taskID + "-result", 0, -1);
        if (null != limit && redisValueList.size() < limit) {
            CountDownLatch countDownLatch = new CountDownLatch(limit);
            GlobalData.taskID2LatchMap.put(taskID, countDownLatch);
            countDownLatch.await(waitSeconds, TimeUnit.SECONDS);
            redisValueList = redis.lRange(taskID + "-result", 0, -1);
        }
        List<String> result = redisValueList.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        log.info("[getTask] result={} taskID={}, limit={}", result, taskID, limit);

        // log device time for data analyse
        int size = redisValueList.size();
        String taskKey = String.format(GlobalData.taskTimesKey, taskID, 1);
        if (limit == null || size >= limit) {
            timeLogService.defaultSetLog(taskID, "server_result_num", String.valueOf(size));
        } else {
            timeLogService.defaultSetLog(taskID, "server_result_num", "-1");
        }
        timeLogService.setTimeLog(GlobalData.SERVER, taskID, "1_ackTime", JSON.toJSONString(GlobalData.taskAck2InfoMap.get(taskKey)));
        timeLogService.setTimeLog(GlobalData.SERVER, taskID, "1_sendError", JSON.toJSONString(GlobalData.task2ErrorInfoMap.get(taskKey)));
        timeLogService.setTimeLog(GlobalData.SERVER, taskID, "1_resultTime", JSON.toJSONString(GlobalData.taskResult2InfoMap.get(taskKey)));
        timeLogService.setTimeLog(GlobalData.SERVER, taskID, "1_UUIDInfo", JSON.toJSONString(GlobalData.task2InfoMap.get(taskKey)));
        GlobalData.task2InfoMap.remove(taskKey);
        GlobalData.taskID2LatchMap.remove(taskID);
        GlobalData.task2ErrorInfoMap.remove(taskKey);
        GlobalData.DEBUG_LAST_RESULT_NUM = redisValueList.size();

        return result;
    }

    @Override
    @TimeLogAop(type="server", desc="getDLResult")
    public List<String> getFLResult(String taskID, Integer waitSeconds) {
        List<Object> redisValueList = redis.lRange(taskID + "-result", 0, -1);
        List<String> result = redisValueList.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        log.info("[getDLResult] result={} taskID={}", result, taskID);
        return result;
    }

    @Override
    @TimeLogAop(type="server", desc="aggregateResult")
    public Map<String, Object> aggregateResult(String taskID, String operationType, String operationColumn, List<String> aggregateColumnList, Integer limit, Integer waitSeconds, String otherParam) throws InterruptedException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        // params check
        if (null == aggregateColumnList || aggregateColumnList.isEmpty()) {
            log.info("");
            throw new RuntimeException("aggregateColumnList can not be null");
        }
        if (Strings.isBlank(operationType) || Strings.isBlank(operationColumn)) {
            log.info("");
            throw new RuntimeException("operation can not be null");
        }

        List<Object> redisValueList = redis.lRange(taskID + "-result", 0, -1);
        if (null != limit && redisValueList.size() < limit) {
            CountDownLatch countDownLatch = new CountDownLatch(limit);
            GlobalData.taskID2LatchMap.put(taskID, countDownLatch);
            countDownLatch.await(waitSeconds, TimeUnit.SECONDS);
            redisValueList = redis.lRange(taskID + "-result", 0, -1);
        }

        DataFrame<Object> df = new DataFrame<>();
        Map<String, List<Object>> aggregateMap = new HashMap<>();

        for (Object rawData : redisValueList) {
            Map<String, Object> mapData = JSON.parseObject(String.valueOf(rawData), Map.class);
            for (Map.Entry<String, Object> entry : mapData.entrySet()) {
                if (aggregateMap.containsKey(entry.getKey())) {
                    aggregateMap.get(entry.getKey()).addAll(JSON.parseObject(String.valueOf(entry.getValue()), List.class));
                } else {
                    aggregateMap.put(entry.getKey(), JSON.parseObject(String.valueOf(entry.getValue()), List.class));
                }
            }
        }

        for (Map.Entry<String, List<Object>> entry : aggregateMap.entrySet()) {
            df.add(entry.getKey(), entry.getValue());
        }

        for (String aggregateColumn : aggregateColumnList) {
            df = df.groupBy(aggregateColumn);
        }

        try {
            Class<?> clazz = df.getClass();
            Method method = clazz.getMethod(operationType);
            if (Strings.isBlank(otherParam)) {
                df = (DataFrame<Object>) method.invoke(df);
            } else {
                df = (DataFrame<Object>) method.invoke(df, Double.valueOf(otherParam));
            }
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new RuntimeException("method not found");
        }

        for (Object rawColumn : df.columns()) {
            String column = String.valueOf(rawColumn);
            if (!column.equals(operationColumn) && !aggregateColumnList.contains(column)) {
                df = df.drop(column);
            }
        }
        return df2Map(df, aggregateColumnList, operationColumn);
    }


    @TimeLogAop(type="server", desc="generateDex")
    private List<String> generateDex(String taskDirPath, String libDirPath, String taskID) {
        if (null == taskDirPath) {
            log.error("[generateDex] no taskDirPath");
            return new ArrayList<>();
        }
        File taskDirPathFile = new File(taskDirPath);
        if (!taskDirPathFile.isDirectory()) {
            log.error("[generateDex] path={} is not dir", taskDirPath);
            return new ArrayList<>();
        }
        File[] subFileList = taskDirPathFile.listFiles();
        if (null == subFileList || subFileList.length == 0) {
            log.error("[generateDex] no subFile");
            return new ArrayList<>();
        }
        List<Path> fileList = Arrays.stream(subFileList)
                .map(File::getPath)
                .filter(jarPath -> jarPath.endsWith(".class"))
                .map(Paths::get)
                .collect(Collectors.toList());
        if (null != libDirPath) {
            File libDirPathFile = new File(libDirPath);
            if (libDirPathFile.isDirectory()) {
                File[] subLibFileList = libDirPathFile.listFiles();
                if (null != subLibFileList && subLibFileList.length != 0) {
                    Arrays.stream(subLibFileList).map(File::getPath)
                            .filter(libPath -> libPath.endsWith(".jar"))
                            .forEach(libPath -> fileList.add(Paths.get(libPath)));
                }
            }
        }
        Path outputPath = Paths.get(taskDirPath);
        boolean result = DexUtils.generateDexByD8(outputPath, fileList);
        if (!result) {
            return new ArrayList<>();
        }
        subFileList = taskDirPathFile.listFiles();
        return Arrays.stream(subFileList)
                .map(File::getPath)
                .filter(jarPath -> jarPath.endsWith(".dex"))
                .collect(Collectors.toList());
    }

    private Map<String, Object> df2Map(DataFrame<Object> df, List<String> keyList, String value) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Object> valueList = df.col(value);
        List<List<Object>> tempData = keyList.stream().map(df::col).collect(Collectors.toList());
        for (Integer index=0; index < valueList.size(); index++) {
            StringBuilder key = new StringBuilder();
            String split = "";
            for (List<Object> data : tempData) {
                key.append(split);
                key.append(data.get(index));
                split = "-";
            }
            result.put(key.toString(), valueList.get(index));
        }
        return result;
    }

    private void checkCreateParams(int limitModelNum, String taskType) {
        if (taskType.equals("FL")) {
            if (limitModelNum == 0) {
                throw new RuntimeException("FL type should special limitModelNum");
            }
        }
    }
}
