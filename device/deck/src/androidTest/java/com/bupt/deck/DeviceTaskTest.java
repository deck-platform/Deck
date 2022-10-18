package com.bupt.deck;

import android.content.Context;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.bupt.deck.devicetask.DeviceTask;
import com.bupt.deck.websocket.WebSocketConn;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;
import deck.wrapper.ContextWrapper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DeviceTaskTest {
    String TAG = "DeviceTaskTest-Deck";
    private String testDir;
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        testDir = context.getExternalFilesDir(null).getAbsolutePath() + "/test";
    }

    private Long getFileAndRun(String subDir, String... fileNames) throws Exception {
        DeviceTask deviceTask = new DeviceTask();
        ContextWrapper contextWrapper = new ContextWrapper(context);

        // Execution time measurement
        // Start
        Long st = System.currentTimeMillis();
        String dexPath = "";
        for (String filename : fileNames) {
            dexPath += testDir + subDir + "/" + filename + File.pathSeparator;
        }
        Log.i(TAG, "getFileAndRun: multiple file dexPath " + dexPath);
        PathClassLoader classLoader = new PathClassLoader(dexPath, getClass().getClassLoader());
        // assertNotNull(classLoader);
        Class<?> cls = classLoader.loadClass("dextest");
        Object instance = cls.newInstance();
        Method run = cls.getDeclaredMethod("run", ContextWrapper.class);
        Object retStr = run.invoke(instance, contextWrapper);
        // End
        Long end = System.currentTimeMillis();
        Log.i(TAG, "getFileAndRun: measure time for load and run " + fileNames[0] + ", duration " +
                (end - st) + "ms");

        assertNotNull(retStr);
        Log.i(TAG, "pathClassLoaderWithLibSearchPathTest: run result = " + retStr.toString());

        return end - st;
    }

    private void printStatsOfArrayList(List<Long> lst) {
        LongSummaryStatistics stats = lst.stream().mapToLong((x) -> x).summaryStatistics();
        Log.d(TAG, "printStatsOfArrayList: " + stats);
    }

    // Test for execution time of aggregated .dex and .zip files
    @Test
    public void pathClassLoaderForAggregateDex() throws Exception {
        String subDir = "/aggregate";
        File file = new File(testDir + subDir, "classes.dex");
        assertTrue(file.exists());

        List<Long> durationsForZip = new ArrayList<>();
        List<Long> durationsForDex = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Long durationForZip = getFileAndRun(subDir, "d8-compiled-output.zip");
            durationsForZip.add(durationForZip);
            Long durationForDex = getFileAndRun(subDir, "classes.dex");
            durationsForDex.add(durationForDex);
        }

        Log.i(TAG, "pathClassLoaderForAggregateDex: zip statistics");
        printStatsOfArrayList(durationsForZip);
        Log.i(TAG, "pathClassLoaderForAggregatedDex: dex statistics");
        printStatsOfArrayList(durationsForDex);
    }

    // Test for execution of split http-request.dex, dextest.dex
    @Test
    public void pathClassLoaderForSplitDex() throws Exception {
        String subDir = "/split";
        File file = new File(testDir + subDir, "dextest.dex");
        assertTrue(file.exists());

        Long duration = getFileAndRun(subDir, "dextest.dex", "http-req.dex");
        Log.i(TAG, "pathClassLoaderForSplitDex: use dex dependency " + duration + " ms");
    }

    @Test
    public void pathClassLoaderForDexZip() throws Exception {
        String subDir = "/img";
        File file = new File(testDir + subDir, "rt-jdk7.zip");
        Log.i(TAG, "pathClassLoaderForDexZip: filePath " + file.getAbsolutePath());
        assertTrue(file.exists());

        Long duration = getFileAndRun(subDir, "classes.dex", "rt-jdk7.zip");
        Log.i(TAG, "pathClassLoaderForDexZip: use dex and zip " + duration + " ms");
    }

    @Test
    public void pathClassLoaderForMultipleDex() throws Exception {
        String subDir = "/multiple-dex";
        File file = new File(testDir + subDir, "classes3.dex");
        Log.i(TAG, "pathClassLoaderForMultipleDex: filePath " + file.getAbsolutePath());
        assertTrue(file.exists());

        Long duration = getFileAndRun(subDir,
                "classes.dex",
                "classes2.dex",
                "classes3.dex",
                "classes4.dex");
        Log.i(TAG, "pathClassLoaderForDexZip: use dex and zip " + duration + " ms");
    }

    // Use DexClassLoader to load dependent jar files
    @Test
    public void dexClassLoaderForSplitDexJar() throws Exception {
        String subDir = "/split";
        String jarFileName = "http-request-6.0.jar";
        File file = new File(testDir + subDir, jarFileName);
        assertTrue(file.exists());

        String dexAbsPath = testDir + subDir + "/" + "dextest.dex";
        String jarAbsPath = testDir + subDir + "/" + jarFileName;
        String path = jarAbsPath + File.pathSeparator + dexAbsPath;
        DexClassLoader dexClassLoader = new DexClassLoader(path, null, null, getClass().getClassLoader());
        Class<?> cls = dexClassLoader.loadClass("dextest");
        Object instance = cls.newInstance();
        Method run = cls.getDeclaredMethod("run", ContextWrapper.class);
        ContextWrapper contextWrapper = new ContextWrapper(context);
        Object retStr = run.invoke(instance, contextWrapper);
        Log.i(TAG, "dexClassLoaderForSplitDexJar: " + retStr.toString());
    }

    @Test
    public void constructAndSendMetricsMsgTest() {
        DeviceTask deviceTask = new DeviceTask();
        deviceTask.setTaskID("SQLQuery-1622721282193-task");

        assertNotNull(deviceTask.constructMetricsMsg(context));
        Log.i(TAG, "constructMetricsMsgTest: " + deviceTask.constructMetricsMsg(context));

        WebSocketConn.getInstance(context).getWsConnection().send(deviceTask.constructMetricsMsg(context));
    }
}