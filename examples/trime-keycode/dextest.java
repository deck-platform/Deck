import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;
import deck.wrapper.ContextWrapper;
import deck.wrapper.SQLiteDatabaseWrapper;


public class dextest {
    
    public String run(ContextWrapper contextWrapper) {
        String query_long = "Select keycode, count(*) as count from inputinfo group by keycode order by count DESC limit 10";
        String DBName = "input_info.db";
        SQLiteDatabaseWrapper db = SQLiteDatabaseWrapper.getDB(contextWrapper, DBName);
        Map<String, List<String>> ret = db.sqlQuery(query_long, "keycode", "count");
        db.close();
        System.out.println(ret);
        Map<String, List<Long>>  result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : ret.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().map(Long::valueOf).collect(Collectors.toList()));
        }
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject.toString();
    }
}
