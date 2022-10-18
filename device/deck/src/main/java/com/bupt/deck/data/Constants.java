package com.bupt.deck.data;

public class Constants {
    // Key In Data object passed between different workers
    public static final String keyForDeviceTask = "DeviceTask";
    public static final String keyForDeviceTaskID = "DeviceTaskID";
    public static final String keyForDeviceTaskResult = "DeviceTaskResult";
    public static final String keyForDeviceTaskDistributeTimes = "DeviceTaskDistributeTimes";

    // Key in SharedPreferences for logging database entries offset which has been sent
    public static final String keyForWSConnChecking = "WSConnCheckingDBOffset";
    public static final String keyForWSConnEvent = "WSConnEventDBOffset";
}
