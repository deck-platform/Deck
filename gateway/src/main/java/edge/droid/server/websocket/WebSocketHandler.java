package edge.droid.server.websocket;

import edge.droid.server.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Component
@ServerEndpoint("/websocket")
@Slf4j
public class WebSocketHandler {

    private static WebSocketService webSocketService;

    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        WebSocketHandler.webSocketService = webSocketService;
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        webSocketService.handleOpen(session);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        webSocketService.handleClose(session);
    }

    @OnMessage(maxMessageSize=2560000)
    public void onMessage(String message) {
        //log.info("[onMessage] message={}", message);
        webSocketService.handleMessage(message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.warn("[onError] hashcode={}", session.hashCode());
        log.info("[onError] session status={}", session.isOpen());
        error.printStackTrace();
    }
}
