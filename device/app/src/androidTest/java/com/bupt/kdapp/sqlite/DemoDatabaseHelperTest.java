package com.bupt.kdapp.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DemoDatabaseHelperTest {

    String TAG = "DatabaseHelper-Deck";

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
    }

    List<String> sqlQuery(SQLiteDatabase db, String query, String columnName) {
        List<String> ret = new ArrayList<>();
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                ret.add(c.getString(c.getColumnIndex(columnName)));
                c.moveToNext();
            }
        }
        return ret;
    }

    @Test
    public void openDBTest() {
        DemoDatabaseHelper dbHelper = new DemoDatabaseHelper(this.context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.i(TAG, "openDBTest: get db: " + db.toString());

        {
            String queryAllTable = "SELECT name FROM sqlite_master WHERE type='table'";
            List<String> allTables = sqlQuery(db, queryAllTable, "name");
            Log.i(TAG, "openDBTest: tables: " + allTables);
        }
        {
            String queryOnArtist = "SELECT * FROM Artist LIMIT 10";
            List<String> first10Artists = sqlQuery(db, queryOnArtist, "Name");
            Log.i(TAG, "openDBTest: artists: " + first10Artists);
        }
    }
}