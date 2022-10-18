import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import deck.wrapper.ContextWrapper;
import deck.wrapper.SQLiteDatabaseWrapper;
import org.json.JSONObject;


public class dextest {

    public String getDomain(String url) {
        String pattern = "/(?!/)";
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher(url);
        List<Integer> list = new ArrayList<>();
        while (matcher.find()){
            list.add(matcher.start());
        }
        return url.substring(0, list.get(1)+1);
    }
    
    public String run(ContextWrapper contextWrapper) {
        String query_long = "Select url, page_load_time from pageloadinfo where url REGEXP '(^(http|HTTP)(s|S)?:\\/\\/[a-zA-Z0-9\\.]+\\/)(.*)' and page_load_time > 10";
        String DBName = "page_load_info.db";
        SQLiteDatabaseWrapper db = SQLiteDatabaseWrapper.getDB(contextWrapper, DBName);
        Map<String, List<String>> ret = db.sqlQuery(query_long, "url", "page_load_time");
        db.close();
        System.out.println(ret);
        Map<String, List<Object>> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : ret.entrySet()) {
            if (entry.getKey().equals("page_load_time")) {
                result.put(entry.getKey(), entry.getValue().stream().map(Float::valueOf).collect(Collectors.toList()));
            } else {
                // get domain
                result.put(entry.getKey(), entry.getValue().stream().map(this::getDomain).collect(Collectors.toList()));
            }
        }
        JSONObject jsonObject = new JSONObject(result);
        return jsonObject.toString();
    }
}
