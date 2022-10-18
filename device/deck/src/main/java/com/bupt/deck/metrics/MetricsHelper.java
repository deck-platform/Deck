package com.bupt.deck.metrics;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsHelper {
    static final String TAG = "Deck-MetricsHelper";
    private static final String PREF_NAME = "Deck";

    public static String getString(Context context, String key) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(key, null);
    }

    public static boolean getBoolean(Context context, String key) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(key, false);
    }

    public static int getInt(Context context, String key) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(key, 0);
    }

    public static void put(Context context, String key, String val) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(key, val).apply();
    }

    public static void put(Context context, String key, int val) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putInt(key, val).apply();
    }

    public static void put(Context context, String key, boolean val) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(key, val).apply();
    }

    public static void put(Context context, String key, long val) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putLong(key, val).apply();
    }

    // For key which is combined of {metrics_keu}_{taskID}_{times}, return {metrics_key}
    // Such as: Dex-execution-end_SQLQuery-1622721282193-task_1, return Dex-execution-end
    private static String getMetricName(String keyInSharedPreferences) {
        return keyInSharedPreferences.split("_")[0];
    }

    /**
     * Get Metrics from SharedPreferences
     *
     * @param context    Android application context
     * @param metricsKey Each task MetricsKey in: {MetricsType}_{taskID}_{times}
     *                   like "Receive-msg-DEXFILE_{taskID}_1", "Is-wifi-connected_{taskID}_3"
     * @return A map which holding all matched metrics
     */
    public static Map<String, Object> queryMetricsByKey(Context context, String metricsKey) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Map<String, Object> ret = new HashMap<>();
        // After send metrics to server, remove all these metrics
        List<String> toBeRemoved = new ArrayList<>();
        Map<String, ?> map = sp.getAll();
        for (Map.Entry<String, ?> item : map.entrySet()) {
            if (item.getKey().endsWith(metricsKey)) {
                ret.put(getMetricName(item.getKey()), item.getValue());
                toBeRemoved.add(item.getKey());
            }
        }
        // Remove items in toBeRemoved
        for (String key : toBeRemoved) {
            sp.edit().remove(key).apply();
        }
        return ret;
    }
}
