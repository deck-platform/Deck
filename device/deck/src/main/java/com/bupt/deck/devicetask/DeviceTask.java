package com.bupt.deck.devicetask;

import android.content.Context;
import android.util.Log;

import com.bupt.deck.data.GlobalData;
import com.bupt.deck.devicestatus.DeviceStatusCollector;
import com.bupt.deck.metrics.MetricsHelper;
import com.bupt.deck.utils.FileHelper;
import com.bupt.deck.utils.UUIDHelper;
import com.bupt.deck.websocket.WebSocketMsgType;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

import dalvik.system.PathClassLoader;
import deck.wrapper.ContextWrapper;

/*
 * TODO: Save DeviceTask object into local storage.
 */
public class DeviceTask {
    private static final String className = "dextest";
    private static final String flClassName = "com.osfans.trime.fl.dextest";
    private static final String dexFieldInMessage = "dexfilecontent";
    String TAG = "DeviceTask-Deck";
    private String taskID;
    private String dexfileStorageDir;
    private String dexfileName;
    private String result;
    private Boolean isTaskDone;
    // The same task may be invoke multiple times
    private int localInvokeTimes;
    // The same taskID may be distribute to this device multiple times
    private int distributeTimes;

    public DeviceTask() {
    }

    // TODO: support saving multiple dex files
    // Construct a device task, extract dexfileContent from message and dump to dex file to
    // appInternalPath
    public DeviceTask(JSONObject jsonObj, String appInternalPath) throws JSONException {
        dexfileStorageDir = appInternalPath;
        taskID = jsonObj.getString("taskid");
        isTaskDone = false;
        // Used for log and compared with distributeTimes
        localInvokeTimes = 0;
        distributeTimes = jsonObj.getInt("times");
        deck.data.GlobalData.permissionInfo = new Gson().fromJson(jsonObj.getString("permissionInfo"), Map.class);
        System.out.println("testPermission:" + jsonObj.getString("permissionInfo"));

        // Check dex file content length
        String dexFileInStr = jsonObj.getString(dexFieldInMessage);

        // Decode dex file content: str in ascii -> base64 bytes -> decoded bytes
        byte[] dexfileContent = FileHelper.stringToByteArray(dexFileInStr);

        dexfileName = taskID + ".dex";
        // Save dexfilecontent field in app internal storage
        FileHelper.writeFile(dexfileStorageDir, dexfileContent, dexfileName);
    }

    /**
     * Construct metrics message
     *
     * @param taskID          taskID used to construct SharedPreferences metrics key
     * @param distributeTimes Task invoke times
     * @param context         Android application context
     * @return WebSocket message in string
     */
    public static String constructMetricsMsg(String taskID, int distributeTimes, Context context) {
        // metricsKey equals to {taskID}_{times} format, so the value of taskid field in metrics
        // message should be metricsKey
        String metricsKey = taskID + "_" + distributeTimes;
        Map<String, Object> metricsData = MetricsHelper.queryMetricsByKey(context, metricsKey);
        try {
            return new JSONObject()
                    .put("uuid", UUIDHelper.getInstance(context).getUniqueID())
                    .put("msgtype", WebSocketMsgType.METRICS.getMsgTypeName())
                    .put("taskid", taskID)
                    .put("times", distributeTimes)
                    .put("deviceName", DeviceStatusCollector.getDeviceName())
                    .put("data", new JSONObject(metricsData))
                    .toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String constructDexFileAck(String taskID, int distributeTimes, Context context) {
        try {
            return new JSONObject()
                    .put("uuid", UUIDHelper.getInstance(context).getUniqueID())
                    .put("msgtype", WebSocketMsgType.DEXFILE_ACK.getMsgTypeName())
                    .put("times", distributeTimes)
                    .put("taskid", taskID)
                    .toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Deserialize string to deviceTask object.
    public static DeviceTask deserializeFromJson(String jsonInString) {
        return new Gson().fromJson(jsonInString, DeviceTask.class);
    }

    // Serialize DeviceTask object to string.
    public static String serializeFromDeviceTask(DeviceTask deviceTask) {
        return new Gson().toJson(deviceTask);
    }

    public int getDistributeTimes() {
        return distributeTimes;
    }

    public void setDistributeTimes(int distributeTimes) {
        this.distributeTimes = distributeTimes;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public Boolean isTaskDone() {
        return isTaskDone;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setDexfileStorageDir(String dexfileStorageDir) {
        this.dexfileStorageDir = dexfileStorageDir;
    }

    public void setDexfileName(String dexfileName) {
        this.dexfileName = dexfileName;
    }

    // Load dex file from external storage and run
    // METRICS: how long does it take to load and run method in .dex file
    public void runDeviceTask(Context context) {
        long executeSt = System.currentTimeMillis();
        localInvokeTimes++;

        // Useless invoke times checking
        // if (localInvokeTimes != this.distributeTimes) {
        //     Log.e(TAG, "runDeviceTask: local invoke times " + localInvokeTimes + " does not" +
        //             " equals to distributeTimes " + distributeTimes);
        //     return;
        // }

        if (isTaskDone) {
            Log.i(TAG, "runDeviceTask: Task " + taskID + " has finished, ignore");
            return;
        }

        // Load test.dex from external storage
        File file = new File(dexfileStorageDir, dexfileName);
        if (file.exists()) {

            // TODO: Do not use PathClassLoader to reduce twice IO operations from sdcard,
            //  but when adding dexfileContent field in DeviceTask object, this object can not
            //  be passed into Data object caused by more than 10240 bytes when serialized.
            //  Can we save dexfileContent in a global dict or other memory space?
            // InMemoryDexClassLoader classLoader = new InMemoryDexClassLoader(
            //         GlobalData.taskID2DexByteBuffer.get(taskID),
            //         getClass().getClassLoader()
            // );

            PathClassLoader classLoader = new PathClassLoader(file.getAbsolutePath(),
                    getClass().getClassLoader());
            try {
                Class<?> cls;
                try {
                    cls = classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    cls = classLoader.loadClass(flClassName);
                }
                Object instance = cls.newInstance();

                // Log all methods
                for (Method method : cls.getDeclaredMethods()) {
                    Log.d(TAG, "runDeviceTask: method: " + method.getName());
                }

                // Get run method
                Log.i(TAG, "runDeviceTask: " + taskID + ": " + this.localInvokeTimes +
                        " times run locally, " + this.distributeTimes + " times distributed");

                // Impl 1
                Method run = cls.getDeclaredMethod("run", ContextWrapper.class);
                // Impl 2
                // Method run = extractRunMethod(cls, ContextWrapper.class);

                ContextWrapper contextWrapper = new ContextWrapper(context);
                Object retStr = run.invoke(instance, contextWrapper);
                Log.i(TAG, "loadDexfileAndExecute: Get run result " + retStr);

                // Save task execution result and mark this DeviceTask Done
                if (retStr == null) {
                    result = "Result string on device is null";
                } else {
                    result = retStr.toString();
                }
                isTaskDone = true;

            } catch (Exception e) {
                e.printStackTrace();
                // If execute error, response to server
                Log.e(TAG, "loadDexfileAndExecute: " + e.toString());
            }
        } else {
            Log.e(TAG, "runDeviceTask: dexfile " + file.getAbsolutePath() + " does not " +
                    "exist", new Exception("dexfile does not exist"));
        }

        // Add metrics to SharedPreferences only when task done
        if (isTaskDone) {
            long executeEnd = System.currentTimeMillis();
            GlobalData.executorService.submit(() -> {
                MetricsHelper.put(context.getApplicationContext(),
                        "Dex-execution-start_" + taskID + "_" + this.distributeTimes,
                        executeSt);
                MetricsHelper.put(context.getApplicationContext(),
                        "Dex-execution-end_" + taskID + "_" + this.distributeTimes,
                        executeEnd);
                MetricsHelper.put(context.getApplicationContext(),
                        "Is-screen-on_" + taskID + "_" + this.distributeTimes,
                        DeviceStatusCollector.isScreenOn(context.getApplicationContext()));
                MetricsHelper.put(context.getApplicationContext(),
                        "Is-wifi-connected_" + taskID + "_" + this.distributeTimes,
                        DeviceStatusCollector.isWifiConnected(context.getApplicationContext()));
                MetricsHelper.put(context.getApplicationContext(),
                        "Is-charging_" + taskID + "_" + this.distributeTimes,
                        DeviceStatusCollector.isDeviceCharging(context.getApplicationContext()));
                MetricsHelper.put(context.getApplicationContext(),
                        "Battery-percentage_" + taskID + "_" + this.distributeTimes,
                        DeviceStatusCollector.getBatteryPercentage(context.getApplicationContext()));
            });
        }
    }

    // Extract "run" method with "ContextWrapper" parameter
    private Method extractRunMethod(Class cls, Class paramCls) throws NoSuchMethodException {
        Class<?>[] argClass = {paramCls};
        return cls.getDeclaredMethod("run", argClass);
    }

    // Extract "run" method with "ContextWrapper" and variable parameter: Object[]
    private Method extractRunMethodAlternate(Class cls) throws NoSuchMethodException {
        Class<?>[] argClass = {ContextWrapper.class, Object[].class};
        return cls.getDeclaredMethod("run", argClass);
    }

    public String constructMetricsMsg(Context context) {
        String metricsKey = this.getTaskID() + "_" + this.distributeTimes;
        Map<String, Object> metricsData = MetricsHelper.queryMetricsByKey(context, metricsKey);
        try {
            return new JSONObject()
                    .put("uuid", UUIDHelper.getInstance(context).getUniqueID())
                    .put("msgtype", WebSocketMsgType.METRICS.getMsgTypeName())
                    .put("taskid", taskID)
                    .put("times", this.distributeTimes)
                    .put("deviceName", DeviceStatusCollector.getDeviceName())
                    .put("data", new JSONObject(metricsData))
                    .toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    // ACK message for task: taskID from device: uuid after receiving "DEXFILE" message
    private String constructDexFileAck(Context context) {
        try {
            return new JSONObject()
                    .put("uuid", UUIDHelper.getInstance(context).getUniqueID())
                    .put("msgtype", WebSocketMsgType.DEXFILE_ACK.getMsgTypeName())
                    .put("times", this.distributeTimes)
                    .put("taskid", taskID)
                    .toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setTaskDone(Boolean taskDone) {
        isTaskDone = taskDone;
    }
}
