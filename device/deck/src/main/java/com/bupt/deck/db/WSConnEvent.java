package com.bupt.deck.db;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.bupt.deck.devicestatus.DeviceStatusCollector;

@Entity
public class WSConnEvent {
    public static final String CONNECT = "connect";
    public static final String DISCONNECT = "disconnect";
    public static final String RECONNECT = "reconnect";
    public static final String FAILURE = "failure";
    public static final String CLOSING = "closing";
    public static final String CLOSED = "closed";


    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "ts")
    public long timestamp;

    @ColumnInfo(name = "event")
    public String event;

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "is_screen_on")
    public boolean isScreenOn;

    @ColumnInfo(name = "is_wifi_connected")
    public boolean isWifiConnected;

    @ColumnInfo(name = "battery_percentage")
    public int batteryPercentage;

    @ColumnInfo(name = "is_charging")
    public boolean isCharging;

    public WSConnEvent() {
    }

    @Ignore
    public WSConnEvent(String event, String message, Context context) {
        this.timestamp = System.currentTimeMillis();
        this.event = event;
        this.message = message;
        this.isScreenOn = DeviceStatusCollector.isScreenOn(context);
        this.isWifiConnected = DeviceStatusCollector.isWifiConnected(context);
        this.isCharging = DeviceStatusCollector.isDeviceCharging(context);
        this.batteryPercentage = DeviceStatusCollector.getBatteryPercentage(context);
    }

    @Ignore
    public WSConnEvent(String event, Context context) {
        this.timestamp = System.currentTimeMillis();
        this.event = event;
        this.message = event;
        this.isScreenOn = DeviceStatusCollector.isScreenOn(context);
        this.isWifiConnected = DeviceStatusCollector.isWifiConnected(context);
        this.isCharging = DeviceStatusCollector.isDeviceCharging(context);
        this.batteryPercentage = DeviceStatusCollector.getBatteryPercentage(context);
    }
}

