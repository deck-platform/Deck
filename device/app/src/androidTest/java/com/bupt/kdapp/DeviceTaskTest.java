package com.bupt.kdapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;

import dalvik.system.PathClassLoader;
import deck.wrapper.ContextWrapper;


public class DeviceTaskTest {
    private static final String TAG = "DeviceTaskTestInKDApp";

    private String testDir;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        testDir = context.getFilesDir().getAbsolutePath() + "/test/";
        ContextWrapper wrapper = new ContextWrapper(context);
        Log.i(TAG, "setUp: " + wrapper);
    }

    // Test get specific method with dynamic variables: for example run method in dextest.dex
    @Test
    public void getDynamicVariablesDexMethod() throws Exception {
        Log.i(TAG, "getDynamicVariablesDexMethod: testDir: " + testDir);
        String filename = "classes.dex";
        File dexfile = new File(testDir + filename);
        assertTrue(dexfile.exists());

        PathClassLoader classLoader = new PathClassLoader(dexfile.getAbsolutePath(), ClassLoader.getSystemClassLoader());
        Class<?> cls = classLoader.loadClass("dextest");
        cls.getDeclaredMethod("run", ContextWrapper.class);

        for (Method method : cls.getDeclaredMethods()) {
            Log.i(TAG, "getDynamicVariablesDexMethod: method: " + method.getName() +
                    ", params: " + Arrays.toString(method.getParameterTypes()));
            assertEquals(method.getParameterTypes()[0].getClasses(), ContextWrapper.class);
        }

        // Class<?>[] argClasses = {ContextWrapper.class, int.class, int.class};
        // Method runMultipleVariables = cls.getDeclaredMethod("run", argClasses);
        Method runMultipleVariables = cls.getDeclaredMethod("run", ContextWrapper.class);
        Log.i(TAG, "getDynamicVariablesDexMethod: extract run method: " + runMultipleVariables.getName());

    }


}
