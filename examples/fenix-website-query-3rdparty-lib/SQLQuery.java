import deck.Task;
import deck.DeviceFuture;
import deck.Deck;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;


public class SQLQuery {
    static String TAG = "fenix-SQLQuery";
    public static void main(String[] args) {

        Deck.init("fenix.kddev.host:9998");
        try {
            String curDir = Paths.get("").toAbsolutePath().toString() + File.separatorChar;
            Task task = Deck.constructTask(new File("dextest.java"), new File(curDir), curDir, "fenixSQLQuery");
            String taskId = task.getTaskid();
            System.out.println("===taskID: " + taskId);
            List<DeviceFuture> futures = task.run();
            List<String> results = Deck.get(futures, 3, 60);
            for (String s : results) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
