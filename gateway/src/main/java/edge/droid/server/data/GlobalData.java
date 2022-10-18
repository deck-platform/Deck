package edge.droid.server.data;

import edge.droid.server.model.DeviceInfo;
import edge.droid.server.model.DeviceResponse;
import edge.droid.server.model.SendErrorModel;
import edge.droid.server.model.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Configuration
public class GlobalData {

    /**
     * Store all connected UUID information
     */
    public static Map<String, DeviceInfo> uuid2ClientMap = new ConcurrentHashMap<>();

    /**
     * Record the UUID of the returned ack in the task
     */
    public static Map<String, Set<String>> taskID2UuIDListMap = new ConcurrentHashMap<>();

    /**
     * Task time log for each UUID in each round
     */
    public static Map<String, Map<String, Long>> taskIDWithUuID2timeMap = new ConcurrentHashMap<>();

    /**
     * CountDownLatch of default tasks waiting for results
     */
    public static Map<String, CountDownLatch> taskID2LatchMap = new ConcurrentHashMap<>();

    /**
     * CountDownLatch of FL tasks waiting for results
     */
    public static Map<String, CountDownLatch> taskIDFL2LatchMap = new ConcurrentHashMap<>();

    /**
     * The CountDownLatch for each round of waiting for FL
     */
    public static Map<String, CountDownLatch> taskIDTimesFL2LatchMap = new ConcurrentHashMap<>();

    /**
     * Store task information
     */
    public static Map<String, Task> taskID2Task = new ConcurrentHashMap<>();

    /**
     * Time of receiving client ack in each round
     */
    public static Map<String, List<DeviceResponse>> taskAck2InfoMap = new ConcurrentHashMap<>();
    /**
     * Time of receiving client result in each round
     */
    public static Map<String, List<DeviceResponse>> taskResult2InfoMap = new ConcurrentHashMap<>();
    /**
     * Error of sending client task in each round
     */
    public static Map<String, List<SendErrorModel>> task2ErrorInfoMap = new ConcurrentHashMap<>();
    /**
     * UUID list of each round of sending tasks
     */
    public static Map<String, String> task2InfoMap = new ConcurrentHashMap<>();

    public static Set<String> debugValidUuidSet = new HashSet<>();


    public static String taskTimesKey = "%s_%d";
    public static String timeLogField = "%s_%s_%s_timeLog"; // "{type}(server/android/user)-{taskID}-{real_key}-time_log"
    public static String timeLogUuIDField = "%s_%s_%s_%s_timeLog"; // "{type}(server/android/user)_{taskID}_{uuID}_{times}_timeLog"
    public static String timeLogUuIDFieldOverLimit = "%s_%s_%s_%s_timeLogOverLimit"; // "{type}(server/android/user)_{taskID}_{uuID}_{times}_timeLog-overLimit"
    public static String timeLogKey = "%s_timeLog"; // "{taskID}-time_log"
    public static String REPORT_KEY = "android_%s_report";
    public static String TaskIDWithUuID = "%s_%s_%s";
    public static String UUID_INFO_KEY = "UUID_INFO_KEY";
    public static String UUID_INFO_KEY_WITH_DATE = "UUID_INFO_KEY_%s";

    public static String SERVER = "server";
    public static String ANDROID = "android";
    public static String USER = "user";

    public static int CORE_POOL_SIZE = 33; // default thread core pool size
    public static String DL_CMD_STR = "aggregateModel.out %s %s"; // FL data aggregation command
    public static String DEFAULT_FL_FILE = "2_mnist.snapshot.mnn"; // default file name of FL in android
    public static Integer DEBUG_LAST_RESULT_NUM = 0;

    // todo add more blacklist
    public static List<String> BLACK_IMPORT_LIST = new ArrayList<String>(){{
        add("java.lang.reflect");
        add("android");
    }};
}
