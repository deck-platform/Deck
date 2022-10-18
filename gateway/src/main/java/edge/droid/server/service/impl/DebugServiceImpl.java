package edge.droid.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edge.droid.server.data.GlobalData;
import edge.droid.server.model.DeviceInfo;
import edge.droid.server.model.FLData;
import edge.droid.server.model.Task;
import edge.droid.server.redis.Redis;
import edge.droid.server.service.CheckerService;
import edge.droid.server.service.DebugService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DebugServiceImpl implements DebugService {

    @Autowired
    private Redis redis;

    @Autowired
    private CheckerService checkerService;

    private static ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("debug-pool-%d").build();
    private static ExecutorService pool = new ThreadPoolExecutor(100, 500,
            2L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(20), threadFactory, new ThreadPoolExecutor.AbortPolicy());

    @Override
    public List<DeviceInfo> getDeviceInfoList() {
        return new ArrayList<DeviceInfo>(GlobalData.uuid2ClientMap.values());
    }

    @Override
    public List<List<String>> getFLTimesResult(String taskID) {
        List<List<String>> result = new ArrayList<>();
        Task task = GlobalData.taskID2Task.get(taskID);
        if (task == null) {
            task = JSON.parseObject(String.valueOf(redis.get(taskID)), Task.class);
        }
        for (int times=1; times <= task.getTrainNum(); times++) {
            List<Object> redisValueList = redis.lRange(taskID + "-result-" + times, 0, task.getLimitModelNum() - 1);
            List<String> resultList = redisValueList.stream()
                    .map(redisValue -> JSON.parseObject(String.valueOf(redisValue), FLData.class))
                    .map(FLData::getResult)
                    .collect(Collectors.toList());
            result.add(resultList);
        }
        return result;
    }

    @Override
    public List<String> getAndroidReportList(String uuID) {
        List<Object> redisValueList = redis.lRange(String.format(GlobalData.REPORT_KEY, uuID), 0, -1);
        return redisValueList.stream().map(String::valueOf).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, Map<String, Long>> testThread(int num) throws InterruptedException {
        Long taskTranBegin = System.currentTimeMillis();
        Map<Integer, Map<String, Long>> result = new HashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(num);
        for (int index = 0; index < num; index++) {
            Long threadBeginTime = System.currentTimeMillis();
            int key = index;
            pool.execute(() -> {
                try {
                    Map<String, Long> key2time = new HashMap<>();
                    key2time.put("tranTimeBegin", System.currentTimeMillis());
                    key2time.put("threadTimeBegin", threadBeginTime);
                    key2time.put("taskTranBegin", taskTranBegin);
                    Thread.sleep(2000);
                    key2time.put("tranTimeEnd", System.currentTimeMillis());
                    result.put(key, key2time);
                    countDownLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        countDownLatch.await();
        //log.info("[testThread] {}
        return result;
    }

    @Override
    public Map<String, Map<String, Long>> getTimeLog(String taskID) {
        Set<String> uuIDList = GlobalData.uuid2ClientMap.keySet();
        Map<String, Map<String, Long>> result = new HashMap<>();
        for (String uuID : uuIDList) {
            if (GlobalData.taskIDWithUuID2timeMap.containsKey(String.format(GlobalData.TaskIDWithUuID, taskID, uuID, 1))) {
                result.put(uuID, GlobalData.taskIDWithUuID2timeMap.get(String.format(GlobalData.TaskIDWithUuID, taskID, uuID, 1)));
            }
        }
        return result;
    }

    @Override
    public Map<Object, Object> getUuIDInfo() {
        return redis.hmGetAll(GlobalData.UUID_INFO_KEY);
    }

    @Override
    public Integer getLastResultNum() {
        return GlobalData.DEBUG_LAST_RESULT_NUM;
    }

    @Override
    public Map<Object, Object> getUuIDInfoWithDate(String date) {
        return redis.hmGetAll(String.format(GlobalData.UUID_INFO_KEY_WITH_DATE, date));
    }

    @Override
    public void gc() {
        System.gc();
    }

    @Override
    public void debugSoot() {

        checkerService.sootInsert("./deck/dex/codec_classes.dex", "");
    }
}
