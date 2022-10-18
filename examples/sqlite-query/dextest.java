import java.util.List;
import java.util.Map;

import deck.wrapper.ContextWrapper;
import deck.wrapper.SQLiteDatabaseWrapper;
import deck.stereotype.DB;


public class dextest {


    @DB("android-devices.db")
    public String run(ContextWrapper contextWrapper) {
        String query_short = "select * from sysinfo where MemFree/MemTotal > 0.15 and" +
            " Total_CPU > 50 and (CPU_0 == 0 or CPU_1 == 0 or CPU_2 == 0 or CPU_3 == 0)";
        String query_long = "SELECT * " +
                "FROM sysinfo sysinfo, screen screen " +
                "WHERE " +
                "(sysinfo.CPU_0 == 0 or sysinfo.CPU_1 == 0 or sysinfo.CPU_2 == 0 or sysinfo.CPU_3 == 0) and " +
                "sysinfo.MemFree/sysinfo.MemTotal > 0.15 and " +
                "sysinfo.SwapFree/sysinfo.SwapTotal < 0.2 and " +
                "sysinfo.Total_CPU > 40;";
        String query = "SELECT * from devices limit 1";
        String DBName = "android-devices.db";
        SQLiteDatabaseWrapper db = SQLiteDatabaseWrapper.getDB(contextWrapper, DBName);
        Map<String, List<String>> ret = db.sqlQuery(query, "name");
        db.close();
        List<String> uuidList = ret.get("name");
        return String.valueOf(uuidList.stream().distinct().count());
    }
}
