import deck.Task;
import deck.DeviceFuture;
import deck.Deck;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;


public class SQLQuery {
    static String TAG = "SQLQuery";
    public static void main(String[] args) {

        Deck.init("149.129.120.139:9999");
        // Deck.init("127.0.0.1:9999");
        try {
            // No third-party jar file is used in dextest.java
            String curDir = Paths.get("").toAbsolutePath().toString() + File.separatorChar;
            Task task = Deck.constructTask(new File("dextest.java"), new File(curDir + ".." + File.separatorChar), curDir, "SQLQuery");
            String taskId = task.getTaskid();
            System.out.println("===taskID: " + taskId);
            List<DeviceFuture> futures = task.run();
            // Wait for 10 seconds to get at least 3 results
            List<String> results = Deck.get(futures, 1);
            for (String s : results) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
