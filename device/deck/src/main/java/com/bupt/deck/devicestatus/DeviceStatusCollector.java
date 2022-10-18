package com.bupt.deck.devicestatus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.PowerManager;

import com.jaredrummler.android.device.DeviceName;


public class DeviceStatusCollector {

    private static final String deviceName = DeviceName.getDeviceName();

    // ref: https://developer.android.com/training/monitoring-device-state/battery-monitoring
    public static boolean isDeviceCharging(Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                (status == BatteryManager.BATTERY_STATUS_FULL);
    }

    // ref: https://stackoverflow.com/questions/2474367/how-can-i-tell-if-the-screen-is-on-in-android
    public static boolean isScreenOn(Context context) {
        // DisplayManager displayManager = (DisplayManager) GlobalClass.getContext()
        //         .getSystemService(Context.DISPLAY_SERVICE);
        // for (Display display : displayManager.getDisplays()) {
        //     if (display.getState() != Display.STATE_OFF) {
        //         return true;
        //     }
        // }
        // return false;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();
    }

    // ref: https://developer.android.com/training/basics/network-ops/managing
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConnected = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConnected |= networkInfo.isConnected();
            }
        }
        return isWifiConnected;
    }

    // Collect battery percentage after task finished
    public static int getBatteryPercentage(Context context) {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    public static String getDeviceName() {
        return deviceName;
    }
}

