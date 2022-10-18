package com.bupt.deck.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WSConnEvent.class, WSConnChecking.class}, version = 1, exportSchema = false)
public abstract class WSConnDB extends RoomDatabase {
    private static final String DB_NAME = "ws_conn.db";
    private static WSConnDB instance;

    public static synchronized WSConnDB getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    WSConnDB.class,
                    DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract WSConnEventDao wsConnEventDao();

    public abstract WSConnCheckingDao wsConnCheckingDao();
}
