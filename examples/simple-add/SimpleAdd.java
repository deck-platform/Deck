import deck.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;


public class SimpleAdd {
    public static void main(String[] args) {
        Deck.init("149.129.120.139:9999");
        try {
            // No third-party jar file is used in dextest.java
            String curDir = Paths.get("").toAbsolutePath().toString() + File.separatorChar;
            Task task = Deck.constructTask(new File("dextest.java"), new File(curDir + ".." + File.separatorChar), curDir, "SimpleAdd");
            List<DeviceFuture> futures = task.run();
            // Wait for 10 seconds to get at least 3 results
            List<String> results = Deck.get(futures, 3, 10);
            for (String s : results) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
