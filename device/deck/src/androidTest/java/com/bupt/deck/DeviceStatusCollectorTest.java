package com.bupt.deck;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.bupt.deck.devicestatus.DeviceStatusCollector;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DeviceStatusCollectorTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void isDeviceCharging() {
        assertTrue(DeviceStatusCollector.isDeviceCharging(context));
    }

    @Test
    public void isScreenOn() {
        assertTrue(DeviceStatusCollector.isScreenOn(context));
    }

    @Test
    public void isWifiConnected() {
        assertTrue(DeviceStatusCollector.isWifiConnected(context));
    }
}