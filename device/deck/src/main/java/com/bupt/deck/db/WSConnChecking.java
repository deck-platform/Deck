package com.bupt.deck.db;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.bupt.deck.devicestatus.DeviceStatusCollector;

@Entity
public class WSConnChecking {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "ts")
    public long timestamp;

    @ColumnInfo(name = "is_ws_connected")
    public boolean isWSConnected;

    @ColumnInfo(name = "is_screen_on")
    public boolean isScreenOn;

    @ColumnInfo(name = "is_wifi_connected")
    public boolean isWifiConnected;

    @ColumnInfo(name = "battery_percentage")
    public int batteryPercentage;

    @ColumnInfo(name = "is_charging")
    public boolean isCharging;

    public WSConnChecking() {
    }

    @Ignore
    public WSConnChecking(boolean isWSConnected, Context context) {
        this.timestamp = System.currentTimeMillis();
        this.isWSConnected = isWSConnected;
        this.isScreenOn = DeviceStatusCollector.isScreenOn(context);
        this.isWifiConnected = DeviceStatusCollector.isWifiConnected(context);
        this.isCharging = DeviceStatusCollector.isDeviceCharging(context);
        this.batteryPercentage = DeviceStatusCollector.getBatteryPercentage(context);
    }
}
