package deck;


import com.alibaba.fastjson.JSON;
import com.github.kevinsawicki.http.HttpRequest;
import deck.data.GlobalData;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// Todo find the better way to implement the base class
@Slf4j
public class BaseDroid {

    private static String taskID;
    private static String gatewayURL;
    // When pass this to get method, indicating server to return all results
    private static final int GET_RETURN_ALL_RESULT = -1;
    // Default wait seconds for get method
    private static final int DEFAULT_WAIT_SECONDS = 10;

    public static String getGatewayURL() {
        return gatewayURL;
    }

    public static String getTaskID() {
        return taskID;
    }

    public static void init(String url) {
        gatewayURL = url;
        GlobalData.gatewayURL = url;
    }

    // Specify source java file and corresponding jar files, compile to .class file,
    // Then submit single class file to gateway.
    // TODO: Pass class DeviceExecutor::run as parameter or other useful
    //  params?
    public static Task constructBaseTask(File file, File libPath, String outputPath, Map<String, Object> requestParams, Object... params)
            throws IOException, InterruptedException {
        // Compile .java file using .jar in libPath, sava all generated .class files in outputPath
        GlobalData.timeMap.put("compileBegin", System.currentTimeMillis());
        boolean result = Utils.compile(file, libPath, outputPath);
        GlobalData.timeMap.put("compileEnd", System.currentTimeMillis());
        if (!result) {
            log.error("[constructTask] compile error");
            throw new RuntimeException("compile error");
        }

        // Get .class files in outputPath
        List<String> classFileList = Utils.getSubFileListFilter(new File(outputPath), ".class", true);
        if (classFileList.size() == 0) {
            throw new RuntimeException("no class file");
        }

        // Get .jar files in libPath
        List<String> jarFileList = Utils.getSubFileListFilter(libPath, ".jar", false);

        // Construct curl command for generating task
        StringBuilder stringBuilder = new StringBuilder()
                .append("curl --location --request POST ")
                .append(gatewayURL)
                .append("/task/create ");
        for (String classFileName : classFileList) {
            stringBuilder.append("--form file=@\"").append(classFileName).append("\" ");
        }
        for (String jarFileName : jarFileList) {
            // Exclude this package when uploading jar files
            if (!jarFileName.contains("deck-1.0-jar-with-dependencies.jar") && !jarFileName.contains("android.jar")) {
                stringBuilder.append("--form lib_file=@\"").append(jarFileName).append("\" ");
            }
        }
        for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
            stringBuilder.append("--form ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\" ");
        }

        String generateTaskCommand = stringBuilder.toString();

        log.debug("[constructTask] command: {}", generateTaskCommand);

        GlobalData.timeMap.put("generateBegin", System.currentTimeMillis());
        Process process = Runtime.getRuntime().exec(generateTaskCommand);
        int resultCode = process.waitFor();
        if (resultCode == 0) {
            GlobalData.timeMap.put("generateEnd", System.currentTimeMillis());
            String resp = Utils.readProcessOutput(process.getInputStream());
            log.info("[constructTask] execute command successfully, get response from gateway: {}", resp);
            JSONObject jsonObj = new JSONObject(resp);
            // Construct a Deck.Task object using response
            Task task = new Task(jsonObj.getString("data"));
            taskID = task.getTaskid();
            return task;
        } else {
            String errMsg = Utils.readProcessOutput(process.getErrorStream());
            log.error("[constructTask] execute command error: {}", errMsg);
            throw new RuntimeException("execute 'generate task' command error");
        }
    }

    // When limit == -1, construct curl command without limit form, which indicating server to return [all] results.
    // Else, server will return at least [limit] results.
    private static String buildGetResultCommand(String taskid, int limit, int waitSeconds) {
        StringBuilder stringBuilder = new StringBuilder()
                .append("curl --location -XGET ")
                .append(gatewayURL)
                .append("/task/get ")
                .append("--form task_id=\"").append(taskid).append("\" ")
                .append("--form wait_seconds=\"").append(waitSeconds).append("\" ");
        if (limit != -1) {
            stringBuilder.append("--form limit=\"").append(limit).append("\" ");
        }
        return stringBuilder.toString();
    }

    // get method should block to get all results
    public static List<String> get(List<DeviceFuture> futures, int limit, int waitSeconds) throws Exception {
        // Only get one deck.DeviceFuture object with taskId in futures
        DeviceFuture future = futures.get(0);
        String taskId = future.getDeviceTaskID();
        String getResultCommand = buildGetResultCommand(taskId, limit, waitSeconds);

        log.debug("[get] command: {}", getResultCommand);
        List<String> result = getListStringRet(getResultCommand);
        sendTimeLog(taskId);
        return result;
    }

    public static String get(DeviceFuture future, int limit, int waitSeconds) throws Exception {
        String taskId = future.getDeviceTaskID();
        String getResultCommand = buildGetResultCommand(taskId, limit, waitSeconds);

        log.debug("[get] command: {}", getResultCommand);

        return getStringRet(getResultCommand);
    }

    public static String get(DeviceFuture future, int limit) throws Exception {
        return get(future, limit, DEFAULT_WAIT_SECONDS);
    }

    public static List<String> get(List<DeviceFuture> futures, int limit) throws Exception {
        return get(futures, limit, DEFAULT_WAIT_SECONDS);
    }

    public static String get(DeviceFuture future) throws Exception {
        // Make limit = -1 to return all results
        return get(future, GET_RETURN_ALL_RESULT, DEFAULT_WAIT_SECONDS);
    }

    public static List<String> get(List<DeviceFuture> futures) throws Exception {
        return get(futures, GET_RETURN_ALL_RESULT, DEFAULT_WAIT_SECONDS);
    }

    // Get task results by taskID
    public static String getByTaskID(String taskId) throws Exception {
        String getResultCommand = buildGetResultCommand(taskId, GET_RETURN_ALL_RESULT, DEFAULT_WAIT_SECONDS);
        log.debug("[getByTaskID] command: {}", getResultCommand);

        return getStringRet(getResultCommand);
    }

    private static String getStringRet(String getResultCommand) throws IOException, InterruptedException {
        GlobalData.timeMap.put("getResultBegin", System.currentTimeMillis());
        Process process = Runtime.getRuntime().exec(getResultCommand);
        int resultCode = process.waitFor();
        if (resultCode == 0) {
            String resp = Utils.readProcessOutput(process.getInputStream());
            log.info("[get] execute command successfully, get response from gateway: {}", resp);
            JSONObject jsonObj = new JSONObject(resp);
            // Get result from json
            return jsonObj.get("data").toString();
        } else {
            String errMsg = Utils.readProcessOutput(process.getErrorStream());
            log.error("[get] execute command error: {}", errMsg);
            throw new RuntimeException("execute 'get task result' command error");
        }
    }

    // TODO: Finish this function
    private static List<String> getListStringRet(String getResultCommand) throws IOException, InterruptedException {
        List<String> result = new LinkedList<>();
        GlobalData.timeMap.put("getBegin", System.currentTimeMillis());
        Process process = Runtime.getRuntime().exec(getResultCommand);
        int resultCode = process.waitFor();
        if (resultCode == 0) {
            GlobalData.timeMap.put("getEnd", System.currentTimeMillis());
            String resp = Utils.readProcessOutput(process.getInputStream());
            log.info("[get] execute command successfully, get response from gateway: {}", resp);
            JSONObject jsonObj = new JSONObject(resp);
            // Get result from json
            Object ret = jsonObj.get("data");
            result.add(ret.toString());
            return result;
        } else {
            String errMsg = Utils.readProcessOutput(process.getErrorStream());
            log.error("[get] execute command error: {}", errMsg);
            throw new RuntimeException("execute 'get task result' command error");
        }
    }

    // OKHttp support
    // private static void sendTimeLog(String taskId) throws IOException {
    //     OkHttpClient client = new OkHttpClient();
    //     RequestBody formBody = new FormBody.Builder()
    //             .add("task_id", taskId)
    //             .add("log_data_map", JSON.toJSONString(GlobalData.timeMap))
    //             .build();
    //
    //     Request request = new Request.Builder()
    //             .url("http://" + gatewayURL + "/task/createTimeLog")
    //             .post(formBody)
    //             .build();
    //
    //     log.debug("[sendTimeLog] command: {}", request);
    //     Response response = client.newCall(request).execute();
    //     log.info("[sendTimeLog] response: {}", response.body().string());
    // }

    public static void sendTimeLog(String taskId) throws IOException {
        Map<String, String> data = new HashMap<String, String>();
        data.put("task_id", taskId);
        data.put("log_data_map", JSON.toJSONString(GlobalData.timeMap));
        String url = "http://" + gatewayURL + "/task/timelog/create";
        HttpRequest httpRequest = HttpRequest.post(url).form(data);
        log.info("[sendTimeLog] response: {}", httpRequest.body());
    }
}
