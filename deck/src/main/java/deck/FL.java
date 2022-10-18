package deck;

import com.alibaba.fastjson.JSON;
import com.github.kevinsawicki.http.HttpRequest;
import deck.data.GlobalData;
import deck.data.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FederatedLearning operations
 */
@Slf4j
public class FL extends BaseDroid {

    public static String getModel(List<DeviceFuture> futures, int waitSeconds) throws IOException {
        String aggResult = makeGatewayAggModel(futures, waitSeconds);
        return aggResult;
    }

    public static List<String> getResult(List<DeviceFuture> futures, int waitSeconds) {
        // todo overwrite the get result method
        return null;
    }

    public static Task constructTask(File file, File libPath, String outputPath, String taskDesc, int limitModelNum, int trainNum, Object... params)
            throws IOException, InterruptedException {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("limit_model_num", limitModelNum);
        requestParams.put("description", taskDesc);
        requestParams.put("task_type", "FL");
        requestParams.put("train_num", trainNum);
        return constructBaseTask(file, libPath, outputPath, requestParams, params);
    }

    public static List<String> getTrainTraces(List<DeviceFuture> futures) {
        String taskId = futures.get(0).getDeviceTaskID();
        return getTrainTracesByTaskID(taskId);
    }

    public static List<String> getTrainTracesByTaskID(String taskId) {
        String url = "http://" + Deck.getGatewayURL() + "/task/fl/train/trace/get";
        HttpRequest httpRequest = HttpRequest.get(url, true, "task_id", taskId);
        if (!httpRequest.ok()) {
            log.error("[getTrainTraces] request error, code+{}", httpRequest.code());
            throw new RuntimeException("request error");
        }
        Response<List<String>> response = JSON.parseObject(httpRequest.body(), Response.class);
        return response.getData();
    }

    public static String makeGatewayAggModel(List<DeviceFuture> futures, int waitSeconds) throws IOException {
        GlobalData.timeMap.put("getBegin", System.currentTimeMillis());
        String taskId = futures.get(0).getDeviceTaskID();
        String url = "http://" + Deck.getGatewayURL() + "/task/fl/get";
        HttpRequest httpRequest = HttpRequest.get(url, true, "task_id", taskId, "wait_seconds", waitSeconds);
        if (!httpRequest.ok()) {
            log.error("[makeGatewayAggModel] request error, code+{}", httpRequest.code());
            throw new RuntimeException("request error");
        }
        Response<String> response = JSON.parseObject(httpRequest.body(), Response.class);
        GlobalData.timeMap.put("getEnd", System.currentTimeMillis());
        log.info("[makeGatewayAggModel] response code:{}, message:{}", response.getCode(), response.getMessage());
        sendTimeLog(taskId);
        return response.getData();
    }
}
