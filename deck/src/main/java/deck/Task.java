package deck;

import deck.data.GlobalData;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class Task {
    private Boolean isCompiledToDex;
    private String pathToDex;
    private List<Object> params;

    public String getTaskid() {
        return taskid;
    }

    private String taskid;

    public Task(String taskid) {
        this.taskid = taskid;
    }

    public List<DeviceFuture> run() throws Exception {
        String runTaskCommand = "curl --location --request POST " + GlobalData.gatewayURL + "/task/run " +
                "--form task_id=\"" + taskid + "\"";

        System.out.println(runTaskCommand);
        log.debug("[run] command: {}", runTaskCommand);
        GlobalData.timeMap.put("runBegin", System.currentTimeMillis());
        Process process = Runtime.getRuntime().exec(runTaskCommand);
        int resultCode = process.waitFor();
        if (resultCode == 0) {
            GlobalData.timeMap.put("runEnd", System.currentTimeMillis());
            // TODO: UUID should returned from gateway which indicates this task is running on what devices
            //  such as:
            //   {
            //       "code":200,
            //       "data":
            //           {
            //               "info": "GetBookIDTask-1621388300206-task tran success",
            //               "uuids": ["uuid1", "uuid2", "uuid3", "uuid4"],
            //           },
            //       "message":"success"
            //   }
            // Get gateway response from process.getInputStream()
            String resp = Utils.readProcessOutput(process.getInputStream());
            log.info("[run] get response from gateway: {}", resp);
            // Create a list of DeviceFutures and return
            List<DeviceFuture> futures = new LinkedList<DeviceFuture>();
            // Set a temp uuid here, so this DeviceFuture object represents a task run on a device which
            // uuid is "some-uuid"
            DeviceFuture future = new DeviceFuture(taskid, "some-uuid");
            futures.add(future);
            return futures;
        } else {
            String errMsg = Utils.readProcessOutput(process.getErrorStream());
            log.error("[run] execute command error: {}", errMsg);
            throw new RuntimeException("execute 'run task command' error");
        }
    }
}
