package com.bupt.deck.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class PreferenceHelper {
    private static final String PREF_NAME = "Deck_pref";
    private static final String KEY_DEVICE_ID = "device_id";

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

    // Use this method to get a DeviceID stored on mobile storage per installation
    public static String getDeviceId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(KEY_DEVICE_ID)) {
            sharedPreferences
                    .edit()
                    .putString(KEY_DEVICE_ID, UUID.randomUUID().toString())
                    .apply();
        }
        return sharedPreferences.getString(KEY_DEVICE_ID, null);
    }

    /**
     * Get entry offset in database by DBOffsetKey
     *
     * @param context     Application context
     * @param DBOffsetKey All static Strings are in data.Constants, used to find the last sent
     *                    entry offset form database
     * @return Offset in DB
     */
    public static int getDBOffset(Context context, String DBOffsetKey) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!sharedPreferences.contains(DBOffsetKey)) {
            sharedPreferences
                    .edit()
                    .putInt(DBOffsetKey, 0)
                    .apply();
        }
        return sharedPreferences.getInt(DBOffsetKey, 0);
    }

    public static void setDBOffset(Context context, String DBOffsetKey, int hasSentOffset) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(DBOffsetKey, hasSentOffset).apply();
    }
}
