package com.bupt.deck.devicetask;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bupt.deck.data.Constants;
import com.bupt.deck.data.GlobalData;
import com.bupt.deck.metrics.MetricsHelper;
import com.bupt.deck.utils.UUIDHelper;
import com.bupt.deck.websocket.WebSocketConn;
import com.bupt.deck.websocket.WebSocketMsgType;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultSentWorker extends Worker {
    private static final String TAG = "ResultSent-Deck";

    public ResultSentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    // Get taskID and result then send back all message to server
    @NonNull
    @Override
    public Result doWork() {
        String taskID = getInputData().getString(Constants.keyForDeviceTaskID);
        DeviceTask deviceTask = GlobalData.taskIDToDeviceTask.get(taskID);
        if (deviceTask == null) {
            Log.e(TAG, "doWork: Cannot find " + taskID + " in Global Map, return error");
            return Result.failure();
        }
        String result = deviceTask.getResult();
        Log.i(TAG, "doWork: send result back to gateway for task: " + taskID);
        try {
            String msg = new JSONObject()
                    .put("uuid", UUIDHelper
                            .getInstance(this.getApplicationContext())
                            .getUniqueID())
                    .put("taskid", taskID)
                    .put("times", deviceTask.getDistributeTimes())
                    .put("msgtype", WebSocketMsgType.RESULT.getMsgTypeName())
                    .put("result", result)
                    .toString();
            WebSocketConn.getInstance(this.getApplicationContext()).getWsConnection().send(msg);
            // METRICS: timestamp of send results back to gateway
            long resultSentTs = System.currentTimeMillis();
            GlobalData.executorService.submit(() -> {
                MetricsHelper.put(this.getApplicationContext(),
                        "Sendback-result_" + taskID + "_" + deviceTask.getDistributeTimes(),
                        resultSentTs);
            });
            // Put taskID in outputData for future using by MetricsSentWorker, which will use
            // taskid to retrieve metrics in SharedPreferences
            Data outputData = new Data.Builder()
                    .putString(Constants.keyForDeviceTaskID, taskID)
                    .putInt(Constants.keyForDeviceTaskDistributeTimes, deviceTask.getDistributeTimes())
                    .build();
            return Result.success(outputData);
        } catch (JSONException e) {
            Log.e(TAG, "doWork: construct json error: " + e.toString());
            return Result.failure();
        }
    }
}
