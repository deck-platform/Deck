package com.bupt.deck;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.bupt.deck.metrics.MetricsHelper;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class MetricsHelperTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void queryMetricsByTaskID() {
        Map<String, Object> ret = MetricsHelper.queryMetricsByKey(context, "pictureHandler-1622718311053-task_1");
        System.out.println("Deck" + ret.toString());
        Gson gson = new Gson();
        System.out.println("Deck" + gson.toJson(ret));
    }
}