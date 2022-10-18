package com.bupt.deck.checkers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

// Run this scheduler when network fails
public class NetworkChecker extends Worker {
    String TAG = "NetworkCheck-Deck";

    public NetworkChecker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "doWork: Network OK");
        return Result.success();
    }

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest req;
        req = new PeriodicWorkRequest
                .Builder(NetworkChecker.class, 5, TimeUnit.MINUTES)
                .addTag(NetworkChecker.class.getSimpleName())
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(req);
    }

    public static void cancel(Context context) {
        WorkManager
                .getInstance(context)
                .cancelAllWorkByTag(NetworkChecker.class.getSimpleName());
    }

}
