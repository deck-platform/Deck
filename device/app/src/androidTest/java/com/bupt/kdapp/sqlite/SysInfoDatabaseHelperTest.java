package com.bupt.kdapp.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SysInfoDatabaseHelperTest {

    String TAG = "SysInfoDBHelperTest-Deck";

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void sqlQuery() {
        SysInfoDatabaseHelper helper = new SysInfoDatabaseHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();
        {
            String query = "SELECT * FROM sysinfo WHERE MemFree > 80000;";
            Long st = System.currentTimeMillis();
            List<String> ret = helper.sqlQuery(query, "UUID");
            Long end = System.currentTimeMillis();
            Log.i(TAG, "sqlQuery: " + String.join("; ", ret));
            Log.i(TAG, "sqlQuery: Duration = " + (end - st) + " ms");
        }
        {
            String query = "SELECT COUNT(*) FROM sysinfo WHERE MemFree > 80000;";
            Long st = System.currentTimeMillis();
            List<String> ret = helper.sqlQuery(query, null);
            Long end = System.currentTimeMillis();
            Log.i(TAG, "sqlQuery: " + String.join("; ", ret));
            Log.i(TAG, "sqlQuery: Duration = " + (end - st) + " ms");
        }
    }
}