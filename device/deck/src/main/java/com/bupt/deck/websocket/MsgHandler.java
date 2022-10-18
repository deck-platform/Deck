package com.bupt.deck.websocket;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.bupt.deck.data.Constants;
import com.bupt.deck.data.GlobalData;
import com.bupt.deck.devicetask.DeviceTask;
import com.bupt.deck.devicetask.ResultSentWorker;
import com.bupt.deck.devicetask.TaskRunWorker;
import com.bupt.deck.metrics.MetricsHelper;
import com.bupt.deck.metrics.MetricsSentWorker;
import com.bupt.deck.utils.FileHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MsgHandler {
    static String TAG = "WebSocketMsgHandler-Deck";

    /*
     * Return metrics key which is corresponding with msgtype.
     * If msgType == DEXFILE, return "DEXFILE_{taskid}",
     * else if msgType == CANCEL, return "CANCEL_{taskid}", etc.
     * Note that use "_" to connect {taskID} and {msgtype}
     */
    public static String handle(String text, Context context) throws JSONException, Exception {
        // Convert text to json
        JSONObject jsonObj = new JSONObject(text);

        // Check msgtype field and do next processing
        String msgtype = jsonObj.getString("msgtype");

        if (msgtype.equals(WebSocketMsgType.DEXFILE.getMsgTypeName())) {
            return handleDEXFILEMessage(context, jsonObj);
        } else if (msgtype.equals(WebSocketMsgType.REQ_RESULT.getMsgTypeName())) {
            return handleREQREQUESTMessage(context, jsonObj);
        } else if (msgtype.equals(WebSocketMsgType.CANCEL.getMsgTypeName())) {
            return handleCANCELMessage(context, jsonObj);
        }
        return null;
    }

    // Return metrics key after handling DEXFILE message
    private static String handleDEXFILEMessage(Context context, JSONObject jsonObj) throws Exception {
        String msgtype = WebSocketMsgType.DEXFILE.getMsgTypeName();
        String taskid = jsonObj.getString("taskid");
        int distributeTimes = jsonObj.getInt("times");
        Log.i(TAG, "handle: Get task " + taskid + " for " + distributeTimes + " times from server");

        GlobalData.executorService.submit(() -> {
            WebSocketConn.getInstance(context)
                    .getWsConnection()
                    .send(DeviceTask.constructDexFileAck(taskid, distributeTimes, context));
        });

        // Task is first time received by device
        if (distributeTimes == 1) {
            if (!jsonObj.has("dexfilecontent")) {
                throw new Exception("Cannot find dexfilecontent field in message when times = " +
                        jsonObj.getInt("times"));
            }

            // Save all files in files field if exists
            handleFilesField(context, jsonObj);
            handleDexfileContentField(context, jsonObj, taskid, distributeTimes);
            return msgtype + "_" + taskid + "_" + distributeTimes;
        } else {
            // Get DeviceTask Object
            DeviceTask deviceTask = GlobalData.taskIDToDeviceTask.get(taskid);
            // If distributeTimes > 1 but this device has not constructed a DeviceTask object,
            // the gateway should know this info and has dexfileconent field in this message.
            if (deviceTask == null) {
                Log.e(TAG, "handle: get DeviceTask for " + taskid + " failed from GlobalMap, " +
                        "find dexfilecontent field from message");
                deviceTask = new DeviceTask(jsonObj,
                        context.getApplicationContext().getFilesDir().getAbsolutePath());
                GlobalData.taskIDToDeviceTask.put(taskid, deviceTask);
            }
            // For FL specifically
            // Cancel prev worker with the same {taskid} but distributeTimes = {this.distributeTimes-1}
            TaskRunWorker.cancel(context, taskid, distributeTimes - 1);
            // If times field does not equal to 1
            handleFilesField(context, jsonObj);

            deviceTask.setTaskDone(false);
            deviceTask.setDistributeTimes(distributeTimes);
            processDeviceTask(context, taskid, distributeTimes);
            return msgtype + "_" + taskid + "_" + distributeTimes;
        }
    }

    /**
     * Construct DeviceTask object according to dexfilecontent field, then put this object to
     * global map, process task and log metrics to SharedPreferences.
     *
     * @param context         Application context
     * @param jsonObj         WebSocket message in JSON
     * @param taskid          TaskID
     * @param distributeTimes Task distribute times
     * @throws Exception When constructing DeviceTask object, JSON exception may be thrown due to
     *                   json field error
     */
    private static void handleDexfileContentField(Context context,
                                                  JSONObject jsonObj,
                                                  String taskid,
                                                  int distributeTimes) throws JSONException {
        // METRICS: timestamp of constructing a DeviceTask object: deserialize content in
        // message and dump to files
        long st = System.currentTimeMillis();
        DeviceTask deviceTask = new DeviceTask(jsonObj,
                context.getApplicationContext().getFilesDir().getAbsolutePath());
        long end = System.currentTimeMillis();

        // Set distributeTimes to current distributeTimes got from WebSocket message
        deviceTask.setDistributeTimes(distributeTimes);
        GlobalData.taskIDToDeviceTask.put(taskid, deviceTask);
        processDeviceTask(context, taskid, distributeTimes);

        GlobalData.executorService.submit(() -> {
            MetricsHelper.put(context,
                    "Save-dexfile-start_" + taskid + "_" + distributeTimes,
                    st);
            MetricsHelper.put(context,
                    "Save-dexfile-end_" + taskid + "_" + distributeTimes,
                    end);
        });
    }

    private static void handleFilesField(Context context, JSONObject jsonObj) throws Exception {
        if (jsonObj.has("files")) {
            JSONArray files = jsonObj.getJSONArray("files");
            for (int i = 0; i < files.length(); ++i) {
                JSONObject file = files.getJSONObject(i);
                Log.i(TAG, "handleFilesField: get file " + file.getString("filename") +
                        " in message");
                FileHelper.writeFile(context.getApplicationContext().getFilesDir().getAbsolutePath(),
                        file.getString("content"),
                        file.getString("filename"));
            }
        }
    }


    private static String handleREQREQUESTMessage(Context context, JSONObject jsonObj) {
        Log.i(TAG, "handle: Got REQRESULT message from server, query result and resend");
        // TODO: Query result using taskid and return key
        //  ...
        return null;
    }

    private static String handleCANCELMessage(Context context, JSONObject jsonObj) throws Exception {
        String msgtype = WebSocketMsgType.CANCEL.getMsgTypeName();
        String taskid = jsonObj.getString("taskid");
        int distributeTimes = jsonObj.getInt("times");
        Log.i(TAG, "handle: Get CANCEL TASK message from server, cancel task " + taskid);
        TaskRunWorker.cancel(context, taskid, distributeTimes);
        return msgtype + "_" + taskid + "_" + distributeTimes;
    }

    // Worker tag format: {WorkerClassName}_{taskID}_{taskDistributeTimes}
    public static String constructWorkerTag(String className, String taskID, int distributeTimes) {
        return className + "_" + taskID + "_" + distributeTimes;
    }

    private static void processDeviceTask(Context context, String taskID, int distributeTimes) {
        // 0. Declare WorkContinuation object
        WorkContinuation continuation;

        // 1. Build TaskRunRequest
        Data data = new Data.Builder()
                .putString(Constants.keyForDeviceTaskID, taskID)
                .build();
        OneTimeWorkRequest runTaskReq;
        runTaskReq = new OneTimeWorkRequest
                .Builder(TaskRunWorker.class)
                .addTag(constructWorkerTag(TaskRunWorker.class.getSimpleName(), taskID, distributeTimes))
                .setInputData(data)
                .build();
        continuation = WorkManager
                .getInstance(context)
                .beginUniqueWork(
                        constructWorkerTag("UniqueWorkChain", taskID, distributeTimes),
                        ExistingWorkPolicy.KEEP,
                        runTaskReq
                );

        // 2. Build ResultSentRequest
        OneTimeWorkRequest resultSentReq;
        resultSentReq = new OneTimeWorkRequest
                .Builder(ResultSentWorker.class)
                .addTag(constructWorkerTag(ResultSentWorker.class.getSimpleName(), taskID, distributeTimes))
                .build();

        // 3. Build MetricsSentRequest
        OneTimeWorkRequest metricsSentReq;
        metricsSentReq = new OneTimeWorkRequest
                .Builder(MetricsSentWorker.class)
                .addTag(constructWorkerTag(MetricsSentWorker.class.getSimpleName(), taskID, distributeTimes))
                .build();

        // 4. Add ResultSentRequest and MetricsSentRequest to chain and enqueue
        Operation operation = continuation
                .then(resultSentReq)
                .then(metricsSentReq).enqueue();

        // try {
        //     Log.i(TAG, "processDeviceTask: " +
        //             operation.getResult().get(10, TimeUnit.SECONDS));
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }
}
