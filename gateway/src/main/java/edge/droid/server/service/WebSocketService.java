package edge.droid.server.service;

import edge.droid.server.data.MsgType;

import javax.websocket.Session;
import java.io.IOException;

public interface WebSocketService {

    void handleMessage(String message);

    void handleOpen(Session session) throws IOException;

    void handleClose(Session session) throws IOException;

    void reTry(String taskID, MsgType msyType, String uuID);
}
