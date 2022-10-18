import deck.DataFrame;
import deck.Task;
import deck.DeviceFuture;
import deck.Deck;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


public class TrimeSQLQuery {
    static String TAG = "trimeSQLQuery";
    public static void main(String[] args) {

        Deck.init("123.57.235.240:9999");
        try {
            String curDir = Paths.get("").toAbsolutePath().toString() + File.separatorChar;
            Task task = Deck.constructTask(new File("dextest.java"), new File(curDir), curDir, "trimeSQLQuery");
            String taskId = task.getTaskid();
            System.out.println("===taskID: " + taskId);
            List<DeviceFuture> futures = task.run();
            DataFrame dataFrame = Deck.getDataFrame();
            Map<String, Object> result = dataFrame.groupBy("keycode").sum("count").get(20, 60);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
