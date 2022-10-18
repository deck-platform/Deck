package com.bupt.deck;

import androidx.work.Data;

import com.bupt.deck.data.Constants;
import com.bupt.deck.devicetask.DeviceTask;
import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

public class TaskRunWorkerTest {

    Long getTimestamp() {
        // return System.nanoTime();
        return System.currentTimeMillis();
    }

    void log(String info) {
        System.out.println(info);
    }

    Long getDurationSerialization(DeviceTask deviceTask) {
        Long st = getTimestamp();
        String s = DeviceTask.serializeFromDeviceTask(deviceTask);
        Long end = getTimestamp();
        return (end-st);
    }

    DeviceTask constructTask() {
        DeviceTask deviceTask = new DeviceTask();
        deviceTask.setDexfileName("test.dex");
        deviceTask.setDexfileStorageDir("/path/to/dexStorage");
        deviceTask.setResult("test-result");
        deviceTask.setTaskDone(true);
        deviceTask.setTaskID("test-taskid");
        return deviceTask;
    }

    @Test
    public void dataObjTest() {
        {
            log("\nTesting Gson object initialization time");
            Long st = getTimestamp();
            Gson gson = new Gson();
            Long end = getTimestamp();
            log("Duration to initialize Gson Object: " + (end-st) + "ms");
        }
        {
            DeviceTask deviceTask = constructTask();

            log("\nTesting serialization time");
            List<Long> durations = new ArrayList<>();
            for (int i=0; i<100; i++) {
                durations.add(getDurationSerialization(deviceTask));
            }
            LongSummaryStatistics stats = durations.stream()
                    .mapToLong((x) -> x)
                    .summaryStatistics();
            log(stats.toString());
            log(durations.toString());

            log("\nTesting deserialization time");
            String s = DeviceTask.serializeFromDeviceTask(deviceTask);
            Long st = getTimestamp();
            DeviceTask t = DeviceTask.deserializeFromJson(s);
            Long end = getTimestamp();
            log("Before deserialize: " + st);
            log("After deserialize: " + end + ", duration: " + (end - st) + "ms");
        }
        {
            DeviceTask deviceTask = constructTask();

            log("\nTesting se/deserialization and put into Data obj time");
            // Start
            Long st = getTimestamp();
            String s = DeviceTask.serializeFromDeviceTask(deviceTask);
            Data data = new Data.Builder().putString(Constants.keyForDeviceTask, s).build();
            String taskFromData = data.getString(Constants.keyForDeviceTask);
            DeviceTask t = DeviceTask.deserializeFromJson(taskFromData);
            // End
            Long end = getTimestamp();
            log("Before serialize and put into Data: " + st);
            log("After getting from Data and deserialize: " + end);
            log("Duration: " + (end-st) + "ms");
        }
    }
}