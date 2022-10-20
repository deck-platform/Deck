package edge.droid.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import edge.droid.server.data.GlobalData;
import edge.droid.server.data.MsgType;
import edge.droid.server.model.*;
import edge.droid.server.redis.Redis;
import edge.droid.server.service.*;
import edge.droid.server.utils.CmdUtils;
import edge.droid.server.utils.FileUtils;
import edge.droid.server.wrapper.DeviceInfoWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Service
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    @Autowired
    private Redis redis;
    @Autowired
    private WebSocketCommonService webSocketCommonService;
    @Autowired
    private DeviceInfoWrapper deviceInfoWrapper;
    @Autowired
    private TimeLogService timeLogService;
    @Autowired
    private TaskManagerService taskManagerService;
    @Autowired
    private SchedulerService schedulerService;

    @Value("${task.base.path}")
    private String taskDirBasePath;
    @Value("${core.pool.size}")
    private int corePoolSize;
    @Value("${redundancy.interval}")
    private int interval;

    private Map<String, AtomicInteger> taskID2TimesMap = new ConcurrentHashMap<>();

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("times-pool-%d").build();
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(GlobalData.CORE_POOL_SIZE, 100,
            2L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(100), threadFactory, new ThreadPoolExecutor.AbortPolicy());

    private static final int SECONDS = 5 * 60;
    private static final String FL_PREFIX_STRING = "FL";

    @Override
    public void handleMessage(String message) {
        WebSocketMessage webSocketMessage = JSON.parseObject(message, new TypeReference<WebSocketMessage>() {});
        String msgTypeStr = webSocketMessage.getMsgType();
        MsgType msgType = MsgType.getMsgTypeByStr(msgTypeStr);
        if (null == msgType) {
            log.error("[handleMessage] msgType={} error", msgTypeStr);
            return;
        }
        if ((msgType.equals(MsgType.RESULT) && webSocketMessage.getTaskID().startsWith(FL_PREFIX_STRING)) || msgType.equals(MsgType.REPORT)) {
            log.info("[onMessage] type={}, taskID={}, uuID={}, times={}", msgType, webSocketMessage.getTaskID(), webSocketMessage.getUuid(), webSocketMessage.getTimes());
        } else {
            log.info("[onMessage] message={}", message);
        }
        log.info("[onMessage] uuid={}, byte length={}", webSocketMessage.getUuid(), message.getBytes().length);
        switch (msgType) {
            case RESULT:
                handleResult(webSocketMessage);
                break;
            case REQ_DEX_FILE:
                handleRetry(webSocketMessage);
                break;
            case CONNECT:
                break;
            case METRICS:
                handleMetrics(webSocketMessage);
                break;
            case PING:
                handleHeart(webSocketMessage);
                break;
            case DEX_FILE_ACK:
                handleDexFileAck(webSocketMessage);
                break;
            case REPORT:
                handleReport(webSocketMessage);
                break;
            default:
                log.error("[handleMessage] msgType={} error", msgType);
        }
    }

    @Override
    public void handleOpen(Session session) throws IOException {
        log.info("[handleOpen] hashcode={}", session.hashCode());
        String uuid = session.getQueryString();
        if (uuid == null || uuid.equals("")) {
            log.error("[WebSocketHandler] connect no uuid");
            session.close();
            return;
        }
        synchronized (this) {
            if (GlobalData.uuid2ClientMap.containsKey(uuid)) {
                try {
                    Session oldSession = GlobalData.uuid2ClientMap.get(uuid).getSession();
                    oldSession.close();
                    log.info("[handleOpen] uuid={} hashcode={} close", uuid, oldSession.hashCode());
                } catch (IOException e) {
                    log.error("[handleOpen] uuid={} close exception", uuid, e);
                }
            }
            GlobalData.uuid2ClientMap.put(uuid, deviceInfoWrapper.deviceInfo(uuid, session));
        }
        log.info("[WebSocketHandler] connect success, uuid={}", uuid);
    }

    @Override
    public void handleClose(Session session) throws IOException {
        log.info("[handleClose] hashcode={}, uuid={}", session.hashCode(), session.getQueryString());
        String uuid = getKeyBySession(GlobalData.uuid2ClientMap, session);
        if (uuid == null || uuid.equals("")) {
            log.warn("[handleClose] can not get uuid");
            return;
        }
        GlobalData.uuid2ClientMap.remove(uuid);
        session.close();
        log.info("[handleClose] uuid={} close", uuid);
    }

    @Override
    public void reTry(String taskID, MsgType msyType, String uuID) {
//        WebSocketMessage webSocketMessage = webSocketMsgWrapper.webSocketMessage(uuID, msyType, taskID, "", new ArrayList<>());
//        try {
//            sendMessage(uuID, JSON.toJSONString(webSocketMessage));
//        } catch (Exception e) {
//            log.error("[reTry] retry error", e);
//        }
    }

    private void handleDexFileAck(WebSocketMessage webSocketMessage) {
        String uuID = webSocketMessage.getUuid();
        String taskID = webSocketMessage.getTaskID();
        Task task = GlobalData.taskID2Task.get(taskID);

        // FL log whether uuID has receive task dex file
        if ("FL".equals(task.getTaskType())) {
            if (GlobalData.taskID2UuIDListMap.containsKey(taskID)) {
                Set<String> uuIDSet = GlobalData.taskID2UuIDListMap.get(taskID);
                uuIDSet.add(uuID);
            } else {
                if (webSocketMessage.getTimes() == 1) {
                    Set<String> uuIDSet = new HashSet<>();
                    uuIDSet.add(uuID);
                    GlobalData.taskID2UuIDListMap.put(taskID, uuIDSet);
                }
            }
        }

        // log device ack for data analyse
        String taskKey = String.format(GlobalData.taskTimesKey, taskID, webSocketMessage.getTimes());
        GlobalData.taskAck2InfoMap.get(taskKey).add(new DeviceResponse(uuID, System.currentTimeMillis()));
    }

    private void handleResult(WebSocketMessage webSocketMessage) {
        String taskID = webSocketMessage.getTaskID();
        Task task = taskManagerService.getTaskByID(taskID);
        long resultTime = System.currentTimeMillis();

        // log device result time for data analyse
        String uuID = webSocketMessage.getUuid();
        Map<String, Long> logData = GlobalData.taskIDWithUuID2timeMap.get(String.format(GlobalData.TaskIDWithUuID, taskID, uuID, webSocketMessage.getTimes()));
        logData.put("recvResultTime", resultTime);

        String taskKey = String.format(GlobalData.taskTimesKey, taskID, webSocketMessage.getTimes());
        GlobalData.taskResult2InfoMap.get(taskKey).add(new DeviceResponse(webSocketMessage.getUuid(), resultTime));

        if (task.getTaskType().equals("FL")) {
            handleFLResult(webSocketMessage, GlobalData.taskID2Task.get(taskID));
        } else {
            handleDefaultResult(webSocketMessage);
        }
        saveUuIDInfo(uuID, true);
        saveUuIDInfo(uuID, false);
    }

    private void handleRetry(WebSocketMessage webSocketMessage) {
        String taskID = webSocketMessage.getTaskID();
        // todo: handle retry
    }

    private void handleMetrics(WebSocketMessage webSocketMessage) {
        String taskID = webSocketMessage.getTaskID();
        Task task = taskManagerService.getTaskByID(taskID);
        String uuID = webSocketMessage.getUuid();
        int times = webSocketMessage.getTimes();
        if (task.getLimitModelNum() != 0) {
            String taskKey = String.format(GlobalData.taskTimesKey, taskID, times);
            if (taskID2TimesMap.containsKey(taskKey)) {
                if (taskID2TimesMap.get(taskKey).getAndDecrement() <= 0) {
                    log.info("[handleMetrics] taskKey={} atomicInteger={} has saved done", taskKey, taskID2TimesMap.get(taskKey).get());
                    timeLogService.setUuidTimeLog(GlobalData.ANDROID, taskID, uuID, webSocketMessage.getData(), times, true);
                    return;
                }
            } else {
                taskID2TimesMap.put(taskKey, new AtomicInteger(task.getLimitModelNum()-1));
            }

            if (Strings.isBlank(taskID) || Strings.isEmpty(uuID)) {
                log.error("[handleMetrics] uuid={} taskID={} null error", taskID, uuID);
                return;
            }
        }
        timeLogService.setUuidTimeLog(GlobalData.ANDROID, taskID, uuID, webSocketMessage.getData(), times, false);
    }

    private void handleHeart(WebSocketMessage webSocketMessage) {
        String uuID = webSocketMessage.getUuid();
        if (Strings.isBlank(uuID)) {
            log.error("[handleHeart] no uuID");
            return;
        }
        WebSocketMessage response = new WebSocketMessage();
        response.setUuid(uuID);
        response.setMsgType("PONG");
        try {
            webSocketCommonService.sendMessage(uuID, JSON.toJSONString(response));
        } catch (IOException e) {
            log.error("[handleHeart] send pong error, uuid={}", uuID);
        } catch (IllegalStateException illegalStateException) {
            log.warn("[handleHeart] uuID={} send exception", uuID, illegalStateException);
        }
    }

    private void handleReport(WebSocketMessage webSocketMessage) {
        String uuID = webSocketMessage.getUuid();
        String redisKey = String.format(GlobalData.REPORT_KEY, uuID);
        redis.rPush(redisKey, webSocketMessage.getData());
    }

    private void handleFLResult(WebSocketMessage webSocketMessage, Task task) {
        // TODO now way maybe not well, find better way later
        if (corePoolSize != 0 && corePoolSize != pool.getCorePoolSize()) {
            pool.setCorePoolSize(corePoolSize);
        }

        int times = webSocketMessage.getTimes();
        String taskID = task.getTaskID();
        String taskKey = String.format(GlobalData.taskTimesKey, taskID, times);

        // Store the necessary time logs in the task for each UUID
        String uuID = webSocketMessage.getUuid();
        Map<String, Long> logData = GlobalData.taskIDWithUuID2timeMap.get(String.format(GlobalData.TaskIDWithUuID, taskID, uuID, times));
        timeLogService.setUuidTimeLog(GlobalData.SERVER, taskID, uuID, JSON.toJSONString(logData), webSocketMessage.getTimes(), false);
        GlobalData.taskIDWithUuID2timeMap.remove(String.format(GlobalData.TaskIDWithUuID, taskID, uuID, times));

        if (GlobalData.taskIDTimesFL2LatchMap.containsKey(taskKey) && GlobalData.taskIDTimesFL2LatchMap.get(taskKey).getCount() == 0) {
            log.info("[handleFLResult] taskKey={}, uuID={} the result of this round has exceeded the limit, ignore", taskKey, webSocketMessage.getUuid());
            return;
        }

        // Create the desired folder
        String taskDLDir = taskDirBasePath + taskID + "/" + "FLFile/" + times + "/";
        FileUtils.createDirs(taskDLDir);
        String subDataPath = taskDLDir + "data/";
        FileUtils.createDirs(subDataPath);


        String result = webSocketMessage.getResult();
        FLData flData = JSON.parseObject(result, FLData.class);

        // save content to .MNN file
        String filePath = String.format("%s%s.mnn", subDataPath, System.currentTimeMillis());
        try {
            FileUtils.writeByteArrayToFile(filePath, flData.getModel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        flData.setModel(filePath);

        redis.rPush(taskID + "-result-" + times, JSON.toJSONString(flData));
        log.info("[handleFLResult] times={}, FLResult={}", times, JSON.toJSONString(flData));

        // Determine whether there are threads waiting in this round. If not, create a new thread; if there is, subtract 1 from the CountDownLatch
        if (GlobalData.taskIDTimesFL2LatchMap.containsKey(taskKey)) {
            GlobalData.taskIDTimesFL2LatchMap.get(taskKey).countDown();
        } else {
            pool.execute(() -> {
                CountDownLatch countDownLatch = new CountDownLatch(task.getLimitModelNum()-1);
                GlobalData.taskIDTimesFL2LatchMap.put(taskKey, countDownLatch);

                timeLogService.setTimeLog(GlobalData.SERVER, taskID, times + "_UUIDInfo", JSON.toJSONString(GlobalData.task2InfoMap.get(taskKey)));
                GlobalData.task2InfoMap.remove(taskKey);

                boolean awaitResult = false;
                try {
                    awaitResult = countDownLatch.await(SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.error("[handleFLResult] error", e);
                }
                // Store the time log of the last round
                timeLogService.setTimeLog(GlobalData.SERVER, taskID, times + "_resultTime", JSON.toJSONString(GlobalData.taskResult2InfoMap.get(taskKey)));
                timeLogService.setTimeLog(GlobalData.SERVER, taskID, times + "_ackTime", JSON.toJSONString(GlobalData.taskAck2InfoMap.get(taskKey)));
                timeLogService.setTimeLog(GlobalData.SERVER, taskID, times + "_sendError", JSON.toJSONString(GlobalData.task2ErrorInfoMap.get(taskKey)));
                GlobalData.task2ErrorInfoMap.remove(taskKey);
                if (!awaitResult) {
                    log.error("[handleFLResult] wait enough result error, taskID={}", taskID);
                    GlobalData.taskIDTimesFL2LatchMap.remove(taskKey);
                    return;
                }

                // compute result
                String outputFile = String.format("%s%s", taskDLDir, GlobalData.DEFAULT_FL_FILE);
                String cmd = String.format(GlobalData.DL_CMD_STR, subDataPath, outputFile);
                boolean cmdResult = CmdUtils.exec(cmd);
                if (!cmdResult) {
                    log.error("[handleFLResult] cmd={} exec error", cmd);
                    return;
                }

                if (times == task.getTrainNum()) {
                    // Train has done
                    if (GlobalData.taskIDFL2LatchMap.containsKey(taskID)) {
                        GlobalData.taskIDFL2LatchMap.get(taskID).countDown();
                    }
                    GlobalData.taskID2UuIDListMap.remove(taskID);
                } else {
                    // send task again
                    taskManagerService.tranTask(task, outputFile, times + 1);
                    task.setCurrentTimes(times + 1);
                    schedulerService.addScheduler(task, interval, TimeUnit.MILLISECONDS, times + 1);
//                    FileModel fileModel = new FileModel(GlobalData.DEFAULT_FL_FILE, FileUtils.fileToBase64(outputFile));
//                    WebSocketMessage newWebSocketMessage = webSocketMsgWrapper.webSocketMessage("", MsgType.DEX_FILE,
//                            taskID, null, null, times+1, Arrays.asList(fileModel));
//                    String rawMessage = JSON.toJSONString(newWebSocketMessage);
//                    WebSocketMessage newWebSocketMessageWithDex = webSocketMsgWrapper.webSocketMessage("", MsgType.DEX_FILE,
//                            taskID, FileUtils.fileToBase64(task.getDexPath()), FileUtils.generateFileContentByDir(task.getLibPath()), times+1, Arrays.asList(fileModel));
//                    String rawMessageWithDex = JSON.toJSONString(newWebSocketMessageWithDex);
//                    Set<String> uuIDDexList = GlobalData.taskID2UuIDListMap.getOrDefault(taskID, new HashSet<>());
//                    Set<String> uuIDList = GlobalData.uuid2ClientMap.keySet();
//                    GlobalData.task2InfoMap.put(String.format(GlobalData.taskTimesKey, task.getTaskID(), times + 1), JSON.toJSONString(uuIDList));
//                    GlobalData.task2ErrorInfoMap.put(String.format(GlobalData.taskTimesKey, task.getTaskID(), times + 1), new ArrayList<>());
//                    Long taskTranBegin = System.currentTimeMillis();
//                    for (String tempUuID : uuIDList) {
//                        Long threadBeginTime = System.currentTimeMillis();
//                        pool.execute(() -> {
//                            try {
//                                Map<String, Long> key2time = new HashMap<>();
//                                key2time.put("tranTimeBegin", System.currentTimeMillis());
//                                key2time.put("connectDeviceNum", Long.valueOf(uuIDList.size()));
//                                key2time.put("threadTimeBegin", threadBeginTime);
//                                key2time.put("taskTranBegin", taskTranBegin);
//                                if (uuIDDexList.contains(tempUuID)) {
//                                    webSocketCommonService.sendMessage(tempUuID, rawMessage);
//                                } else {
//                                    webSocketCommonService.sendMessage(tempUuID, rawMessageWithDex);
//                                }
//                                key2time.put("tranTimeEnd", System.currentTimeMillis());
//                                GlobalData.taskIDWithUuID2timeMap.put(String.format(GlobalData.TaskIDWithUuID, task.getTaskID(), tempUuID, times+1), key2time);
//                                log.info("[handleFLResult] taskID={} send task to uuID={} succ, msgType={}, times={}", task.getTaskID(), tempUuID, newWebSocketMessage.getMsgType(), newWebSocketMessage.getTimes());
//                            } catch (Exception exception) {
//                                log.error("[handleFLResult] taskID={} , uuID={} exception", task.getTaskID(), tempUuID, exception);
//                                GlobalData.task2ErrorInfoMap.get(String.format(GlobalData.taskTimesKey, task.getTaskID(), times + 1)).add(
//                                        new SendErrorModel(tempUuID, exception.getMessage(), System.currentTimeMillis())
//                                );
//                            }
//                        });
//                    }
                }

                // Record the results of the last round
                List<Object> redisValueList = redis.lRange(taskID + "-result-" + times, 0, task.getLimitModelNum() - 1);
                List<String> resultList = redisValueList.stream()
                        .map(redisValue -> JSON.parseObject(String.valueOf(redisValue), FLData.class))
                        .map(FLData::getResult)
                        .collect(Collectors.toList());
                String roundResult = avgModelResult(resultList);
                redis.rPush(taskID + "-result", roundResult);
            });
        }
    }

    private void handleDefaultResult(WebSocketMessage webSocketMessage) {
        String taskID = webSocketMessage.getTaskID();
        Task task = GlobalData.taskID2Task.get(taskID);
        task.incrResult();
        redis.rPush(webSocketMessage.getTaskID() + "-result", webSocketMessage.getResult());
        log.info("[handleResult] result={}", webSocketMessage.getResult());

        // save taskID-uuID time
        String uuID = webSocketMessage.getUuid();
        Map<String, Long> logData = GlobalData.taskIDWithUuID2timeMap.get(String.format(GlobalData.TaskIDWithUuID, taskID, uuID, webSocketMessage.getTimes()));
        timeLogService.setUuidTimeLog(GlobalData.SERVER, taskID, uuID, JSON.toJSONString(logData), webSocketMessage.getTimes(), false);
        GlobalData.taskIDWithUuID2timeMap.remove(String.format(GlobalData.TaskIDWithUuID, taskID, uuID, webSocketMessage.getTimes()));
        changeCountDownAwait(webSocketMessage.getTaskID());
    }

    private String avgModelResult(List<String> resultList) {
        float lossSum = 0;
        float accSum = 0;
        float trainWeightSum = 0;
        float testWeightSum = 0;
        for (String result : resultList) {
            String[] res = result.split(",");
            float loss = Float.parseFloat(res[0]);
            int trainWeight = Integer.parseInt(res[1]);
            float acc = Float.parseFloat(res[2]);
            int testWeight = Integer.parseInt(res[3]);

            lossSum += trainWeight * loss;
            trainWeightSum += trainWeight;
            accSum += testWeight * acc;
            testWeightSum += testWeight;
        }
        float loss = lossSum / trainWeightSum;
        float acc = accSum / testWeightSum;
        return String.format("%f,%f", loss, acc);
    }

    private void saveUuIDInfo(String uuID, boolean withDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(System.currentTimeMillis());
        Object rawData;
        if (withDate) {
            rawData = redis.hmGet(String.format(GlobalData.UUID_INFO_KEY_WITH_DATE, date), uuID); // get date
        } else {
            rawData = redis.hmGet(GlobalData.UUID_INFO_KEY, uuID);
        }
        UuIDInfo uuIDInfo;
        if (null == rawData) {
            uuIDInfo = new UuIDInfo(1, System.currentTimeMillis());
        } else {
            uuIDInfo = JSON.parseObject(String.valueOf(rawData), UuIDInfo.class);
            uuIDInfo.setReceiveTimes(uuIDInfo.getReceiveTimes() + 1);
            uuIDInfo.setLastReceiveTime(System.currentTimeMillis());
        }
        if (withDate) {
            redis.hmSet(String.format(GlobalData.UUID_INFO_KEY_WITH_DATE, date), uuID, JSON.toJSONString(uuIDInfo));
        } else {
            redis.hmSet(GlobalData.UUID_INFO_KEY, uuID, JSON.toJSONString(uuIDInfo));
        }
    }

    private void changeCountDownAwait(String taskID) {
        if (!GlobalData.taskID2LatchMap.containsKey(taskID)) {
            return;
        }
        GlobalData.taskID2LatchMap.get(taskID).countDown();
    }

    private String getKeyBySession(Map<String, DeviceInfo> mapData, Session session) {
        String key = "";
        for (Map.Entry<String, DeviceInfo> entry : mapData.entrySet()) {
            if (session.equals(entry.getValue().getSession())) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }
}
