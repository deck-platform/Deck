package deck.wrapper;

import deck.data.GlobalData;

public class BaseWrapper {

    public static boolean checkSourcePermission(String type, String sourceName) {
        return GlobalData.permissionInfo.containsKey(type) && GlobalData.permissionInfo.get(type).contains(sourceName);
    }
}
