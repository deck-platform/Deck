import deck.Task;
import deck.DeviceFuture;
import deck.KubeDroid;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;


public class TrimeSQLQuery {
    static String TAG = "trimeSQLQuery";
    public static void main(String[] args) {

        Deck.init("trime.kddev.host:9999");
        try {
            String curDir = Paths.get("").toAbsolutePath().toString() + File.separatorChar;
            Task task = Deck.constructTask(new File("dextest.java"), new File(curDir), curDir, "trimeSQLQuery");
            String taskId = task.getTaskid();
            System.out.println("===taskID: " + taskId);
            List<DeviceFuture> futures = task.run();
            List<String> results = Deck.get(futures, 20, 60);
            for (String s : results) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
