package com.bupt.deck.devicetask;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bupt.deck.data.Constants;
import com.bupt.deck.data.GlobalData;
import com.bupt.deck.metrics.MetricsHelper;
import com.bupt.deck.websocket.MsgHandler;

public class TaskRunWorker extends Worker {
    private static final String TAG = "TaskRun-Deck";

    public TaskRunWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void cancel(Context context, String taskid, int distributeTimes) {
        // Tag format: {WorkerClassName}_{taskID}_{taskDistributeTimes}
        Log.i(TAG, "cancel: " + taskid + " execution for " + distributeTimes + " times");
        WorkManager
                .getInstance(context)
                .cancelAllWorkByTag(MsgHandler.constructWorkerTag(
                        TaskRunWorker.class.getSimpleName(),
                        taskid,
                        distributeTimes
                ));
    }

    // Run task and set DeviceTask as output data
    @NonNull
    @Override
    public Result doWork() {
        String taskID = getInputData().getString(Constants.keyForDeviceTaskID);
        DeviceTask deviceTask = GlobalData.taskIDToDeviceTask.get(taskID);
        if (deviceTask == null) {
            Log.e(TAG, "doWork: cannot find " + taskID + " in Global Map");
            return Result.failure();
        }
        // TODO: (runAfter - runBefore) is the thread time for running this task if successful
        //  ref: https://stackoverflow.com/questions/44789531/how-can-i-measure-thread-specific-time-in-android
        //  ref: https://developer.android.com/reference/android/os/SystemClock.html#currentThreadTimeMillis()
        long runBefore = SystemClock.currentThreadTimeMillis();
        deviceTask.runDeviceTask(this.getApplicationContext());
        long runAfter = SystemClock.currentThreadTimeMillis();

        GlobalData.executorService.submit(() -> {
            long threadTimeRunTask = runAfter - runBefore;
            MetricsHelper.put(getApplicationContext(),
                    "Run-task-thread-time_" + taskID + "_" + deviceTask.getDistributeTimes(),
                    threadTimeRunTask);
        });

        // If this task has not finished due to some error, return failure and the whole task chain
        // will end.
        if (!deviceTask.isTaskDone()) {
            return Result.failure();
        } else {
            // Task has finished, pass taskID to next worker (ResultSentWorker)
            Data outputData = new Data.Builder()
                    .putString(Constants.keyForDeviceTaskID, taskID)
                    .build();
            return Result.success(outputData);
        }
    }
}
