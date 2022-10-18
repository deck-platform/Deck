package edge.droid.server.service;

public interface TimeLogService {

    void setTimeLog(String type, String taskID, String subKey, String value);

    void setUuidTimeLog(String type, String taskID, String uuID, String value, int times, boolean overLimit);

    void defaultSetLog(String taskID, String field, String value);
}
