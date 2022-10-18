package edge.droid.server.wrapper;

import edge.droid.server.model.DeviceInfo;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.text.SimpleDateFormat;

@Service
public class DeviceInfoWrapper {

    public DeviceInfo deviceInfo(String uuID, Session session) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setSession(session);
        deviceInfo.setUuID(uuID);
        long timestamp = System.currentTimeMillis();
        deviceInfo.setConnectTimestamp(timestamp);
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time =format.format(timestamp);
        deviceInfo.setConnectTime(time);
        return deviceInfo;
    }
}
