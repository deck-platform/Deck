package deck;

public class DeviceFuture {
    private String deviceUUID;
    private String deviceTaskID;
    private String result;

    public DeviceFuture(String taskid, String uuid) {
        deviceTaskID = taskid;
        deviceUUID = uuid;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getDeviceTaskID() {
        return deviceTaskID;
    }

    public void setDeviceTaskID(String deviceTaskID) {
        this.deviceTaskID = deviceTaskID;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void retrievalResult() {
        // get retrievalResult from gateway interface
        String ret = "Get from server interface";
        setResult(ret);
    }

}
