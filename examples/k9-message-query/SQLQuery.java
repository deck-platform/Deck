import deck.Task;
import deck.DeviceFuture;
import deck.Deck;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;


public class SQLQuery {
    static String TAG = "k9-SQLQuery";
    public static void main(String[] args) {

        Deck.init("k9.kddev.host:9997");
        try {
            String curDir = Paths.get("").toAbsolutePath().toString() + File.separatorChar;
            Task task = Deck.constructTask(new File("dextest.java"), new File(curDir), curDir, "k9SQLQuery");
            String taskId = task.getTaskid();
            System.out.println("===taskID: " + taskId);
            List<DeviceFuture> futures = task.run();
            List<String> results = Deck.get(futures, 7, 60);
            for (String s : results) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
