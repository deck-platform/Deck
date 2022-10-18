package com.bupt.deck.data;

import com.bupt.deck.devicetask.DeviceTask;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalData {
    // ThreadPool for time-sensitive background tasks
    public static ExecutorService executorService = Executors.newFixedThreadPool(3);

    // Global Map for saving DeviceTask objects
    public static Map<String, DeviceTask> taskIDToDeviceTask = createLRUMap(5);

    // ref: https://stackoverflow.com/questions/11469045/how-to-limit-the-maximum-size-of-a-map-by-removing-oldest-entries-when-limit-rea
    private static <K, V> Map<K, V> createLRUMap(final int maxEntries) {
        return new LinkedHashMap<K, V>(maxEntries * 10 / 7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxEntries;
            }
        };
    }
}
