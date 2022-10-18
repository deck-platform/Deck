import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import deck.wrapper.ContextWrapper;
import deck.wrapper.SQLiteDatabaseWrapper;


public class dextest {
    
    public String run(ContextWrapper contextWrapper) {
        String query_long = "Select avg(input_interval) as inter_avg " +
                "from (" +
                "Select " +
                "(info2.input_ts - info1.input_ts) as input_interval, " +
                "info1.input_ts as info1_input_ts, " +
                "info1.keycode as info1_keycode, " +
                "info2.input_ts as info2_input_ts, " +
                "info2.keycode as info2_keycode " +
                "from " +
                "inputinfo info1, inputinfo info2 " +
                "where " +
                "info2.id = info1.id + 1 and " +
                "input_interval < 1000 " +
                ")";
        String DBName = "input_info.db";
        SQLiteDatabaseWrapper db = SQLiteDatabaseWrapper.getDB(contextWrapper, DBName);
        Map<String, List<String>> ret = db.sqlQuery(query_long, "inter_avg");
        db.close();
        System.out.println(ret);
        Map<String, List<Float>>  result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : ret.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().map(Float::valueOf).collect(Collectors.toList()));
        }
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject.toString();
    }
}
