package com.bupt.kdapp.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class SysInfoDatabaseHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "sysinfo.db";
    private static final int DATABASE_VERSION = 1;

    public SysInfoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public List<String> sqlQuery(String query, String columnName) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> ret = new ArrayList<>();
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                ret.add(c.getString(c.getColumnIndex(columnName)));
                c.moveToNext();
            }
        }
        c.close();
        return ret;
    }
}
