package com.bupt.deck.devicestatus;

import com.bupt.deck.db.WSConnChecking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DeviceStatusReporterTest {

    private WSConnChecking createWSConnCheckingObj() {
        WSConnChecking wsConnChecking = new WSConnChecking();
        wsConnChecking.isWSConnected = true;
        wsConnChecking.timestamp = System.currentTimeMillis();
        wsConnChecking.batteryPercentage = 90;
        wsConnChecking.id = 2;
        wsConnChecking.isCharging = true;
        wsConnChecking.isScreenOn = false;
        wsConnChecking.isWifiConnected = true;
        return wsConnChecking;
    }

    @Test
    public void testCreateJsonArray() {
        List<WSConnChecking> list = new ArrayList<>();
        list.add(createWSConnCheckingObj());
        list.add(createWSConnCheckingObj());
        JSONArray jsonArray = DeviceStatusReporter.createJSONArrayFromList(list);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("WSConnChecking", jsonArray);
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}