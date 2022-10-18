package com.bupt.deck.utils;

import android.content.Context;

public class UUIDHelper {
    private static UUIDHelper mInstance = null;

    private String uniqueID;

    private UUIDHelper(Context context) {
        // uniqueID = UUID.randomUUID().toString();
        // Use deviceId rather than UUID
        uniqueID = PreferenceHelper.getDeviceId(context);
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public static synchronized UUIDHelper getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new UUIDHelper(context);
        }
        return mInstance;
    }
}
