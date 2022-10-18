package deck;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class TaskTest {

    @Test
    public void run() throws Exception {
        File file = new File("xxx.java");
        String desc = "taskCreatedInUnitTest";
        Task task = Deck.constructTask(file, new File(""), "", desc);
        System.out.println(task.getTaskid());
        List<DeviceFuture> futures = task.run();
        assertEquals(1, futures.size());
        for (DeviceFuture future : futures) {
            assertEquals(task.getTaskid(), future.getDeviceTaskID());
        }
    }
}