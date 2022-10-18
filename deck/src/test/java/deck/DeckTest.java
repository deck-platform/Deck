package deck;

import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DeckTest {

    private final String serverURL = "";

    @Test
    public void init() {
        Deck.init(serverURL);
        assertEquals(serverURL, Deck.getGatewayURL());
    }

    @Test
    public void constructTask() throws Exception {
        Deck.init(serverURL);
        String testDirInExample = Paths.get(".").toAbsolutePath().normalize().toString() + "/../example/self-defined-class/";
        System.out.println(testDirInExample);
        File file = new File(testDirInExample + "dextest.java");
        String desc = "taskCreatedInUnitTest";
        System.out.println(Deck.getGatewayURL());
        {
            Task task = Deck.constructTask(file, new File(testDirInExample), testDirInExample, desc);
            assertNotNull(task);
            System.out.println(task.getTaskid());
        }
        {
            Task task = Deck.constructTask(file, null, testDirInExample, desc);
            assertNotNull(task);
            System.out.println(task.getTaskid());
        }
        {
            String simpleAddDir = Paths.get(".").toAbsolutePath().normalize().toAbsolutePath() + "/../example/simple-add/";
            file = new File(simpleAddDir + "dextest.java");
            Task task = Deck.constructTask(file, null, simpleAddDir, "taskCreatedInUnitTest");
            System.out.println(task);
        }
    }

    @Test
    public void get() throws Exception {
        String specifyTaskID = "GetBookIDTask-1620828745107-task";
        Deck.init(serverURL);
        DeviceFuture future = new DeviceFuture(specifyTaskID, "some-uuid");
        String ret = Deck.get(future);
        System.out.println(ret);
        ret = Deck.get(future, 2, 5);
        System.out.println(ret);
        ret = Deck.get(future, 100, 3);
    }
}