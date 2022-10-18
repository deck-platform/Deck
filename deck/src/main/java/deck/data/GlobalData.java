package deck.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalData {

    public static Map<String, Long> timeMap = new ConcurrentHashMap<>();

    public static String gatewayURL;

    public static Map<String, List<String>> permissionInfo = new HashMap<>();
}
