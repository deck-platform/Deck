package com.bupt.deck.websocket;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bupt.deck.db.WSConnChecking;
import com.bupt.deck.db.WSConnDB;

import java.util.concurrent.TimeUnit;

// Periodic worker to build WebSocket connection
public class WSConnectionChecker extends Worker {
    String TAG = "WSConnectionChecker-Deck";

    public WSConnectionChecker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void schedule(Context context) {
        PeriodicWorkRequest req = new PeriodicWorkRequest
                .Builder(WSConnectionChecker.class, 20, TimeUnit.MINUTES)
                .addTag(WSConnectionChecker.class.getSimpleName())
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WSConnectionChecker.class.getSimpleName(),
                ExistingPeriodicWorkPolicy.KEEP,
                req);
    }

    public static void cancel(Context context) {
        WorkManager
                .getInstance(context)
                .cancelAllWorkByTag(WSConnectionChecker.class.getSimpleName());
    }

    @NonNull
    @Override
    public Result doWork() {
        if (WebSocketConn.getInstance(this.getApplicationContext()).isDisconnected()) {
            Log.i(TAG, "doWork: WebSocket connection checker: disconnected");
            WSConnDB.getInstance(this.getApplicationContext()).wsConnCheckingDao().insert(
                    new WSConnChecking(false, this.getApplicationContext())
            );
            WebSocketConn.getInstance(this.getApplicationContext()).connect();
        } else {
            WSConnDB.getInstance(this.getApplicationContext()).wsConnCheckingDao().insert(
                    new WSConnChecking(true, this.getApplicationContext())
            );
            Log.i(TAG, "doWork: WebSocket connection checker: connected");
        }
        return Result.success();
    }
}
