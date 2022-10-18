package edge.droid.server.service;

import edge.droid.server.model.DeviceInfo;
import edge.droid.server.model.UuIDInfo;

import java.util.List;
import java.util.Map;

public interface DebugService {

    List<DeviceInfo> getDeviceInfoList();

    List<List<String>> getFLTimesResult(String taskID);

    List<String> getAndroidReportList(String uuID);

    Map<Integer, Map<String, Long>> testThread(int num) throws InterruptedException;

    Map<String, Map<String, Long>> getTimeLog(String taskID) throws InterruptedException;

    Map<Object, Object> getUuIDInfo();

    Integer getLastResultNum();

    Map<Object, Object> getUuIDInfoWithDate(String date);

    void gc();

    void debugSoot();
}
