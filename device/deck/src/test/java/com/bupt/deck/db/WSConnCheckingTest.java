package com.bupt.deck.db;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;

import junit.framework.TestCase;

import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WSConnCheckingTest extends TestCase {
    @Test
    public void testWSConnCheckingObjSerialization() {
        WSConnChecking wsConnChecking = new WSConnChecking();
        wsConnChecking.isWSConnected = true;
        wsConnChecking.timestamp = System.currentTimeMillis();
        wsConnChecking.batteryPercentage = 90;
        wsConnChecking.id = 2;
        wsConnChecking.isCharging = true;
        wsConnChecking.isScreenOn = false;
        wsConnChecking.isWifiConnected = true;
        System.out.println(wsConnChecking);
        Gson gson = new Gson();
        List<WSConnChecking> lst = new ArrayList<>();
        lst.add(wsConnChecking);
        lst.add(wsConnChecking);
        String fastJsonString = JSON.toJSONString(lst);
        String gsonString = gson.toJson(lst);
        System.out.println(fastJsonString);
        System.out.println(gsonString);

        org.json.JSONObject jsonObject = new org.json.JSONObject();
        try {
            jsonObject.put("key1", fastJsonString);
            jsonObject.put("key2", gsonString);
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}