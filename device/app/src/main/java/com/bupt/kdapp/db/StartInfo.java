package com.bupt.kdapp.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class StartInfo {
    @PrimaryKey(autoGenerate = true)
    public int startId;

    @ColumnInfo(name = "start_ts")
    public Long startTimestamp;

    @ColumnInfo(name = "wifi_connected")
    public boolean isWifiConnected;

    @ColumnInfo(name = "charging")
    public boolean isCharging;

    public StartInfo(int startId, long startTimestamp, boolean isWifiConnected, boolean isCharging) {
        this.startId = startId;
        this.startTimestamp = startTimestamp;
        this.isWifiConnected = isWifiConnected;
        this.isCharging = isCharging;
    }

    /*
     * Room cannot pick a constructor since multiple constructors are suitable. So add @Ignore
     * to this constructor.
     */
    @Ignore
    public StartInfo() {
        this.startTimestamp = System.currentTimeMillis();
        this.isWifiConnected = true;
        this.isCharging = true;
    }
}
