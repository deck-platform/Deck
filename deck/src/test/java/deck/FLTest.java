package deck;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotEquals;

public class FLTest {

    @Test
    public void makeGatewayAggModel() throws IOException {
        List<DeviceFuture> deviceFutureList = Arrays.asList(new DeviceFuture("taskid-0704", ""));
        Deck.init("172.19.48.203:9999");
        String result = FL.makeGatewayAggModel(deviceFutureList, 1);
        assertNotEquals(result, "");
    }

    @Test
    public void getTrainTracesByTaskID() {
        Deck.init("kddev.host:9999");
        String flTaskId = "FLTraining-1626082847410-task";
        List<String> ret = FL.getTrainTracesByTaskID(flTaskId);
        assertNotEquals(ret.size(), 0);
        System.out.println(ret);
    }
}
