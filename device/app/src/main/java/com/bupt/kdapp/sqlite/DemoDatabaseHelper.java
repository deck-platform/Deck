package com.bupt.kdapp.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DemoDatabaseHelper extends SQLiteOpenHelper {
    String TAG = "DatabaseHelper-Deck";

    private static final String DBName = "demo_db.db";
    private static final int Version = 1;

    public DemoDatabaseHelper(Context context) {
        super(context, DBName, null, Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate: ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade: ");
    }
}

