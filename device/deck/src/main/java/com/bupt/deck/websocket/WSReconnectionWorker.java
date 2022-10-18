package com.bupt.deck.websocket;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bupt.deck.data.GlobalData;
import com.bupt.deck.db.WSConnDB;
import com.bupt.deck.db.WSConnEvent;

import java.util.concurrent.TimeUnit;

// WebSocket reconnection schedulers
public class WSReconnectionWorker extends Worker {
    static String TAG = "WSReconnectionWorker-Deck";

    public WSReconnectionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void schedule(Context context) {
        Log.i(TAG, "schedule: WebSocketReconnection schedule is called");
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(WSReconnectionWorker.class)
                // .setConstraints(constraints)
                // .setInitialDelay(10, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .addTag(WSReconnectionWorker.class.getSimpleName())
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "WebSocketReconnectionWorker",
                ExistingWorkPolicy.KEEP,
                oneTimeWorkRequest);
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WSReconnectionWorker.class.getSimpleName());
    }

    // TODO: Should not always return Result.success() if connect fails
    @NonNull
    @Override
    public Result doWork() {
        GlobalData.executorService.submit(() ->
                WSConnDB.getInstance(this.getApplicationContext()).wsConnEventDao().insert(
                        new WSConnEvent(WSConnEvent.RECONNECT, getApplicationContext())
                ));
        if (WebSocketConn.getInstance(this.getApplicationContext()).isDisconnected()) {
            Log.i(TAG, "doWork: WebSocket connection is disconnected, reconnect");
            WebSocketConn.getInstance(this.getApplicationContext()).connect();
        } else {
            Log.i(TAG, "doWork: WebSocket connection is still connected");
        }

        // TODO: how to get connect result from prev connect() method? If WSListener onFailure() be
        //  called after this if statement, return Result.failure() and backoffCriteria would not
        //  work.
        // Current solution: Sleep 3 seconds and then return connection status
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        } catch (InterruptedException e) {
            Log.w(TAG, "doWork: sleeping to wait WS connection status update err: "
                    + e.toString());
        }

        if (WebSocketConn.getInstance(this.getApplicationContext()).isDisconnected()) {
            Log.i(TAG, "doWork: WS is still disconnected, reconnection work failure");
            return Result.retry();
        } else {
            Log.i(TAG, "doWork: WS is connected, reconnection work success");
            return Result.success();
        }
    }
}
