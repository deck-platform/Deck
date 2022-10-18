package com.bupt.deck.websocket;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bupt.deck.utils.UUIDHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class WSHeartBeatWorker extends Worker {
    String TAG = "WSHeartBeatWorker";

    public WSHeartBeatWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void schedule(Context context) {
        // Minimum repeat interval is 15 minutes
        PeriodicWorkRequest req = new PeriodicWorkRequest
                .Builder(WSHeartBeatWorker.class, 15, TimeUnit.MINUTES)
                .addTag(WSHeartBeatWorker.class.getSimpleName())
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WSHeartBeatWorker.class.getSimpleName(),
                ExistingPeriodicWorkPolicy.KEEP,
                req);
    }

    public static void cancel(Context context) {
        WorkManager
                .getInstance(context)
                .cancelAllWorkByTag(WSHeartBeatWorker.class.getSimpleName());
    }

    private String constructPingMsg() {
        try {
            return new JSONObject()
                    .put("uuid", UUIDHelper.getInstance(this.getApplicationContext()).getUniqueID())
                    .put("msgtype", WebSocketMsgType.PING.getMsgTypeName())
                    .toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @NonNull
    @Override
    public Result doWork() {
        WebSocketConn.getInstance(this.getApplicationContext()).getWsConnection().send(constructPingMsg());
        Log.i(TAG, "doWork: send PING message");
        return Result.success();
    }
}
