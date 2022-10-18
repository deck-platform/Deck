package com.bupt.deck.devicestatus;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bupt.deck.data.Constants;
import com.bupt.deck.db.WSConnChecking;
import com.bupt.deck.db.WSConnDB;
import com.bupt.deck.db.WSConnEvent;
import com.bupt.deck.utils.PreferenceHelper;
import com.bupt.deck.utils.UUIDHelper;
import com.bupt.deck.websocket.WebSocketConn;
import com.bupt.deck.websocket.WebSocketMsgType;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeviceStatusReporter extends Worker {

    String TAG = "Deck-DeviceStatusReporter";

    public DeviceStatusReporter(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Once constructReportMsg is called, find entries that are not sent in database and send tp
     * gateway.
     *
     * @param context Application context
     * @return REPORT message in String
     */
    public static String constructReportMsg(Context context) {
        try {
            return new JSONObject()
                    .put("uuid", UUIDHelper.getInstance(context).getUniqueID())
                    .put("msgtype", WebSocketMsgType.REPORT.getMsgTypeName())
                    .put("data", constructReportData(context))
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Construct report data get from database by not-sent-offset read from sharedPreferences.
     *
     * @param context Application context
     * @return Data field in REPORT message in JSONObject type
     */
    public static JSONObject constructReportData(Context context) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("deviceName", DeviceStatusCollector.getDeviceName());
        jsonObject.put("timestamp", System.currentTimeMillis());
        // Limit entry number got from database
        int limit = 40;

        // Get DB entry offset which has been sent in WSConnChecking DB
        int WSConnCheckingOffset = PreferenceHelper.getDBOffset(context, Constants.keyForWSConnChecking);
        List<WSConnChecking> wsConnCheckingEntries = WSConnDB
                .getInstance(context.getApplicationContext())
                .wsConnCheckingDao()
                .getIDLargerThanWithLimit(WSConnCheckingOffset, limit);
        jsonObject.put(WSConnChecking.class.getSimpleName(),
                createJSONArrayFromList(wsConnCheckingEntries));

        PreferenceHelper.setDBOffset(context,
                Constants.keyForWSConnChecking,
                WSConnCheckingOffset + wsConnCheckingEntries.size());

        // Get DB entry offset which has been sent in WSConnEvent DB
        int WSConnEventOffset = PreferenceHelper.getDBOffset(context, Constants.keyForWSConnEvent);
        List<WSConnEvent> wsConnEventEntries = WSConnDB
                .getInstance(context.getApplicationContext())
                .wsConnEventDao()
                .getIDLargerThanWithLimit(WSConnEventOffset, limit);
        jsonObject.put(WSConnEvent.class.getSimpleName(),
                createJSONArrayFromList(wsConnEventEntries));

        PreferenceHelper.setDBOffset(context,
                Constants.keyForWSConnEvent,
                WSConnEventOffset + wsConnEventEntries.size());

        return jsonObject;
    }

    public static <T> JSONArray createJSONArrayFromList(List<T> list) {
        Gson gson = new Gson();
        JSONArray jsonArray = new JSONArray();
        for (T item : list) {
            jsonArray.put(gson.toJson(item));
        }
        return jsonArray;
    }

    public static void schedule(Context context) {
        PeriodicWorkRequest req = new PeriodicWorkRequest
                .Builder(DeviceStatusReporter.class, 45, TimeUnit.MINUTES)
                .addTag(DeviceStatusReporter.class.getSimpleName())
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                DeviceStatusReporter.class.getSimpleName(),
                ExistingPeriodicWorkPolicy.KEEP,
                req);
    }

    public static void cancel(Context context) {
        WorkManager
                .getInstance(context)
                .cancelAllWorkByTag(DeviceStatusReporter.class.getSimpleName());
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        String reportMsg = constructReportMsg(this.getApplicationContext());
        WebSocketConn.getInstance(this.getApplicationContext()).getWsConnection().send(reportMsg);
        Log.i(TAG, "doWork: send REPORT message to gateway successfully");
        return Result.success();
    }
}
