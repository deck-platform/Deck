import deck.Task;
import deck.KubeDroid;
import deck.DeviceFuture;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class HandlePicture {

    public static void main(String[] args) {
        Deck.init("123.57.235.240:9999");
        try {
            // picture handle test
            String curDir = Paths.get("").toAbsolutePath().toString() + File.separatorChar;
            System.out.println(curDir);
            Task task = Deck.constructTask(new File("dextest.java"), new File(curDir), curDir, "pictureHandler");
            String taskId = task.getTaskid();
            System.out.println("===taskID: " + taskId);
            List<DeviceFuture> futures = task.run();
            List<String> results = Deck.get(futures);
            for (String s : results) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
