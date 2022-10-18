package edge.droid.server.service.impl;

import edge.droid.server.data.GlobalData;
import edge.droid.server.model.DeviceInfo;
import edge.droid.server.service.WebSocketCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;

@Service
@Slf4j
public class WebSocketCommonServiceImpl implements WebSocketCommonService {

    @Override
    public void sendMessage(String uuID, String message) throws IOException, IllegalStateException {
        DeviceInfo deviceInfo = GlobalData.uuid2ClientMap.get(uuID);
        if (null == deviceInfo || null == deviceInfo.getSession()) {
            log.error("[sendMessage] uuid={} can not get session", uuID);
            return;
        }
        Session session = deviceInfo.getSession();
        session.getBasicRemote().sendText(message);
    }
}
