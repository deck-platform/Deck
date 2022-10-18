package edge.droid.server.service.impl;

import edge.droid.server.data.GlobalData;
import edge.droid.server.redis.Redis;
import edge.droid.server.service.TimeLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class TimeLogServiceImpl implements TimeLogService {

    @Autowired
    private Redis redis;

    @Override
    public void setTimeLog(String type, String taskID, String subKey, String value) {
        String key = String.format(GlobalData.timeLogKey, taskID);
        String field = String.format(GlobalData.timeLogField, type, taskID, subKey);
        redis.hmSet(key, field, value);
    }

    @Override
    public void setUuidTimeLog(String type, String taskID, String uuID, String value, int times, boolean overLimit) {
        String field = String.format(GlobalData.timeLogUuIDField, type, taskID, uuID, times);
        if (overLimit) {
            field = String.format(GlobalData.timeLogUuIDFieldOverLimit, type, taskID, uuID, times);
        }
        String key = String.format(GlobalData.timeLogKey, taskID);
        redis.hmSet(key, field, value);
    }

    @Override
    public void defaultSetLog(String taskID, String field, String value) {
        String key = String.format(GlobalData.timeLogKey, taskID);
        redis.hmSet(key, field, value);
    }
}
