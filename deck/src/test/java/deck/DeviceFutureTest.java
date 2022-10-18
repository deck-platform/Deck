package deck;

import org.junit.Test;

import static org.junit.Assert.*;

public class DeviceFutureTest {

    @Test
    public void getDeviceUUID() {
        DeviceFuture df = new DeviceFuture("task1", "device1");
        assertEquals("device1", df.getDeviceUUID());
    }

    @Test
    public void setDeviceUUID() {
    }

    @Test
    public void getDeviceTaskID() {
    }

    @Test
    public void setDeviceTaskID() {
    }

    @Test
    public void getResult() {
    }

    @Test
    public void setResult() {
    }

    @Test
    public void retrievalResult() {
        String test1 = "1";
        String test2 = "2";
        testTest(test1, test2);
    }

    public void testTest(String... params) {
        for (String test : params) {
            System.out.println(test);
        }
    }
}