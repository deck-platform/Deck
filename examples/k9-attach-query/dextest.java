import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import deck.stereotype.DB;
import deck.wrapper.ContextWrapper;
import deck.wrapper.SQLiteDatabaseWrapper;
import org.json.JSONObject;


public class dextest {

    public String run(ContextWrapper contextWrapper) {
        String query_long = "Select attachment_count from messageinfo;";
        String DBName = "msg_info.db";
        SQLiteDatabaseWrapper db = SQLiteDatabaseWrapper.getDB(contextWrapper, DBName);
        Map<String, List<String>> ret = db.sqlQuery(query_long, "attachment_count");
        db.close();
        System.out.println(ret);
        Map<String, List<Integer>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : ret.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().map(Integer::valueOf).collect(Collectors.toList()));
        }
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject.toString();
    }
}
