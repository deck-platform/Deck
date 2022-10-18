package com.bupt.deck.metrics;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bupt.deck.data.Constants;
import com.bupt.deck.devicetask.DeviceTask;
import com.bupt.deck.utils.UUIDHelper;
import com.bupt.deck.websocket.WebSocketConn;

public class MetricsSentWorker extends Worker {
    String TAG = "MetricsSentWorker-Deck";

    public MetricsSentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork: send metrics info back to gateway from UUID " +
                UUIDHelper.getInstance(this.getApplicationContext()).getUniqueID());
        String metricsMsg = DeviceTask.constructMetricsMsg(
                getInputData().getString(Constants.keyForDeviceTaskID),
                getInputData().getInt(Constants.keyForDeviceTaskDistributeTimes, -1),
                this.getApplicationContext());
        WebSocketConn.getInstance(this.getApplicationContext()).getWsConnection().send(metricsMsg);
        Log.i(TAG, "doWork: after send metrics at " + System.currentTimeMillis());
        return Result.success();
    }
}
