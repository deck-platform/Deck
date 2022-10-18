package edge.droid.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edge.droid.server.data.GlobalData;
import edge.droid.server.data.MsgType;
import edge.droid.server.model.FileModel;
import edge.droid.server.model.SendErrorModel;
import edge.droid.server.model.Task;
import edge.droid.server.model.WebSocketMessage;
import edge.droid.server.redis.Redis;
import edge.droid.server.service.TaskManagerService;
import edge.droid.server.service.WebSocketCommonService;
import edge.droid.server.utils.FileUtils;
import edge.droid.server.wrapper.WebSocketMsgWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
@Slf4j
public class TaskManagerServiceImpl implements TaskManagerService {

    @Autowired
    private WebSocketCommonService webSocketCommonService;
    @Autowired
    private WebSocketMsgWrapper webSocketMsgWrapper;
    @Autowired
    private Redis redis;

    @Value("${core.pool.size}")
    private int corePoolSize;
    @Value("${fl.multiple}")
    private int MultipleFL;

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("task-pool-%d").build();
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(GlobalData.CORE_POOL_SIZE, 40,
            30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(2000), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    private final int WAIT_SECONDS = 60 * 5;


    @Override
    public boolean tranTask(Task task, String FLModelPath, int times) {
        // TODO now way maybe not well, find better way later
        if (corePoolSize != 0 && corePoolSize != pool.getCorePoolSize()) {
            pool.setCorePoolSize(corePoolSize);
        }
        log.info("[] availableProcessors={}", Runtime.getRuntime().availableProcessors());

        String taskID = task.getTaskID();
        List<FileModel> fileModelList = null;
        if (task.getTaskType().equals("FL") && Strings.isNotBlank(FLModelPath)) {
            // FL type need send model
            fileModelList = Arrays.asList(new FileModel(GlobalData.DEFAULT_FL_FILE, FileUtils.fileToBase64(FLModelPath)));
        }
        String dexPath = task.getDexPath();
        String fileContent = FileUtils.fileToBase64(dexPath);
        if (null == fileContent || fileContent.equals("")) {
            log.error("[tranTask] get file content error, dexPath={}", dexPath);
            return false;
        }
        List<String> libFileContentList = FileUtils.generateFileContentByDir(task.getLibPath());
        WebSocketMessage webSocketMessage = webSocketMsgWrapper.webSocketMessage(
                "", MsgType.DEX_FILE, task.getTaskID(), fileContent, libFileContentList, times, fileModelList, task.getPermissionInfo());
        String rawMessage = JSON.toJSONString(webSocketMessage);
        WebSocketMessage webSocketMessageWithoutDex = webSocketMsgWrapper.webSocketMessage(
                "", MsgType.DEX_FILE, task.getTaskID(), null, null, times, fileModelList, task.getPermissionInfo());
        String rawMessageWithOutDex = JSON.toJSONString(webSocketMessageWithoutDex);

        Set<String> uuIDDexList = GlobalData.taskID2UuIDListMap.getOrDefault(taskID, new HashSet<>());
        Set<String> uuIDList = GlobalData.uuid2ClientMap.keySet();
        if ("FL".equals(task.getTaskType())) {
            uuIDList = filterFLUuID(uuIDList, task.getLimitModelNum());
        }
        if (GlobalData.debugValidUuidSet.size() != 0) {
            uuIDList = GlobalData.debugValidUuidSet;
            log.info("[tranTask:debug] uuidSet={}", uuIDList);
        }
        handleTimeLog(taskID, times, uuIDList);
        Long taskTranBegin = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(uuIDList.size());
        final long uuIDListSize = uuIDList.size();
        for (String uuID : uuIDList) {
            Long threadBeginTime = System.currentTimeMillis();
            pool.execute(() -> {
                try {
                    Map<String, Long> key2time = new HashMap<>();
                    key2time.put("tranTimeBegin", System.currentTimeMillis());
                    key2time.put("connectDeviceNum", uuIDListSize);
                    key2time.put("threadTimeBegin", threadBeginTime);
                    key2time.put("taskTranBegin", taskTranBegin);
                    if (uuIDDexList.contains(uuID)) {
                        webSocketCommonService.sendMessage(uuID, rawMessageWithOutDex);
                    } else {
                        webSocketCommonService.sendMessage(uuID, rawMessage);
                    }
                    key2time.put("tranTimeEnd", System.currentTimeMillis());
                    GlobalData.taskIDWithUuID2timeMap.put(String.format(GlobalData.TaskIDWithUuID, task.getTaskID(), uuID, times), key2time);
                    log.info("[tranTask] taskID={} send task to uuID={} succ, times={}", task.getTaskID(), uuID, times);
                } catch (Exception exception) {
                    log.error("[tranTask] taskID={} , uuID={} exception", task.getTaskID(), uuID, exception);
                    GlobalData.task2ErrorInfoMap.get(String.format(GlobalData.taskTimesKey, task.getTaskID(), times)).add(
                            new SendErrorModel(uuID, exception.getMessage(), System.currentTimeMillis())
                    );
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            return countDownLatch.await(WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception exception) {
            log.error("[tranTask] await result error", exception);
            return false;
        }
    }

    @Override
    public Task getTaskByID(String taskID) {
        Task task = GlobalData.taskID2Task.get(taskID);
        if (task == null) {
            task = JSON.parseObject(String.valueOf(redis.get(taskID)), Task.class);
            GlobalData.taskID2Task.put(taskID, task);
        }
        return task;
    }

    private void handleTimeLog(String taskID, int times, Set<String> uuIDList) {
        String taskKey = String.format(GlobalData.taskTimesKey, taskID, times);
        GlobalData.taskAck2InfoMap.put(taskKey, new ArrayList<>());
        GlobalData.task2ErrorInfoMap.put(taskKey, new ArrayList<>());
        GlobalData.taskResult2InfoMap.put(taskKey, new ArrayList<>());
        GlobalData.task2InfoMap.put(taskKey, JSON.toJSONString(uuIDList));
    }

    private Set<String> filterFLUuID(Set<String> uuIDSet, int limit) {
        List<String> uuIDList = new ArrayList<>(uuIDSet);
        int resultNum = limit * MultipleFL;
        int size = uuIDList.size();
        if (size <= resultNum) {
            return uuIDSet;
        }
        Random rand = new Random(System.currentTimeMillis());
        Set<String> result = new HashSet<>();
        for (int index = 0; index < resultNum; index++) {
            int num;
            do {
                num = rand.nextInt(size);
            } while (result.contains(uuIDList.get(num)));
            result.add(uuIDList.get(num));
        }
        return result;
    }
}
