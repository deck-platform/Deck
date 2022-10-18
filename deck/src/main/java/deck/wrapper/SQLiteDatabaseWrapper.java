package deck.wrapper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteDatabaseWrapper extends BaseWrapper {

    private static final String TYPE_NAME = "DB";

    static class DBHelper extends SQLiteOpenHelper {

        String TAG = "DBHelper-Deck";

        private static final int Version = 1;

        public DBHelper(Context context, String DBName) {
            super(context, DBName, null, Version);
        }

        @Override
        public void onCreate(SQLiteDatabase arg0) {
            Log.i(TAG, "onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
            Log.i(TAG, "onUpgrade");
        }
    }

    public static SQLiteDatabaseWrapper getDB(ContextWrapper contextWrapper, String DBName) {
        if (!checkSourcePermission(TYPE_NAME, DBName)) {
            throw new RuntimeException("DB no permission");
        }
        SQLiteDatabase db = new DBHelper(contextWrapper.getContext(), DBName).getReadableDatabase();
        return new SQLiteDatabaseWrapper(db);
    }

    private final SQLiteDatabase db;

    public SQLiteDatabaseWrapper(SQLiteDatabase db) {
        this.db = db;
    }

    public void close() {
        this.db.close();
    }

    public List<String> getTableNames() {
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tables = new ArrayList<>();
        while (c.moveToNext()) {
            tables.add(c.getString(0));
        }
        c.close();
        return tables;
    }

    private List<String> sqlQuery(String query, String columnName) {
        List<String> ret = new ArrayList<>();
        Cursor c = this.db.rawQuery(query, null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                ret.add(c.getString(c.getColumnIndex(columnName)));
                c.moveToNext();
            }
        }
        c.close();
        return ret;
    }

    /*
     * Return SQLQuery result as Map<col_name, val_list>
     * TODO: Unit test for SQLiteDatabase Class?
     */
    public Map<String, List<String>> sqlQuery(String query, String... columnNames) {
        Map<String, List<String>> ret = new HashMap<>();
        List<String> singleColumnRet;
        for (String columnName : columnNames) {
            singleColumnRet = sqlQuery(query, columnName);
            ret.put(columnName, singleColumnRet);
        }
        return ret;
    }
}
