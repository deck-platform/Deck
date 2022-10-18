package edge.droid.server.controller;


import edge.droid.server.data.GlobalData;
import edge.droid.server.model.DeviceInfo;
import edge.droid.server.model.UuIDInfo;
import edge.droid.server.service.DebugService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Slf4j
@Api
public class DebugController {

    @Autowired
    private DebugService debugService;

    @ApiOperation("Get connecting device list")
    @RequestMapping(value = "/debug/device/get", method = RequestMethod.GET)
    public List<DeviceInfo> getDeviceInfoList() {
        return debugService.getDeviceInfoList();
    }

    @ApiOperation("Get FL's result each round")
    @RequestMapping(value = "/debug/fl/times/get", method = RequestMethod.GET)
    public List<List<String>> getFLTimesResult(@RequestParam("task_id") String taskID) {
        return debugService.getFLTimesResult(taskID);
    }

    @ApiOperation("Get devices' report")
    @RequestMapping(value = "/debug/android/reports/get", method = RequestMethod.GET)
    public List<String> getAndroidReports(@RequestParam("uuid") String uuID) {
        return debugService.getAndroidReportList(uuID);
    }

    @RequestMapping(value = "/debug/thread/test", method = RequestMethod.GET)
    public Map<Integer, Map<String, Long>> testThread(@RequestParam("num") int num) throws InterruptedException {
        return debugService.testThread(num);
    }

    @ApiOperation("Get task time log")
    @RequestMapping(value = "/debug/time/get", method = RequestMethod.GET)
    public Map<String, Map<String, Long>> getTimeInfo(@RequestParam("task_id") String taskID) throws InterruptedException {
        return debugService.getTimeLog(taskID);
    }

    @ApiOperation("Get device return info")
    @RequestMapping(value = "/debug/uuid/info/get", method = RequestMethod.GET)
    public Map<Object, Object> getUuIDInfo() {
        return debugService.getUuIDInfo();
    }

    @ApiOperation("Get last result uuid num")
    @RequestMapping(value = "/debug/last/num/get", method = RequestMethod.GET)
    public Integer getLastResultNum() throws InterruptedException {
        return debugService.getLastResultNum();
    }

    @ApiOperation("Get device return info with date")
    @RequestMapping(value = "/debug/uuid/info", method = RequestMethod.GET)
    public Map<Object, Object> getUuIDInfoWithDate(@RequestParam("date") String date) {
        return debugService.getUuIDInfoWithDate(date);
    }

    @ApiOperation("gc manually")
    @RequestMapping(value = "/debug/gc", method = RequestMethod.GET)
    public void debugSystemGC() {
        debugService.gc();
    }

    @ApiOperation("set valid uuid list")
    @RequestMapping(value = "/debug/uuid", method = RequestMethod.POST)
    public void setValidUuidList(@RequestParam("valid_uuid") Set<String> validUuIDSet,
                                 @RequestParam(value = "type", required = false, defaultValue = "") String type) {
        GlobalData.debugValidUuidSet = validUuIDSet;
        if ("reset".equals(type)) {
            GlobalData.debugValidUuidSet = new HashSet<>();
        }
    }

    @ApiOperation("soot test")
    @RequestMapping(value = "/debug/soot", method = RequestMethod.POST)
    public void debugSootTest() {
        debugService.debugSoot();
    }
}