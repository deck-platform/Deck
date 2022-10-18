package edge.droid.server.service;

import java.io.IOException;

public interface WebSocketCommonService {

    void sendMessage(String uuID, String message) throws IOException, IllegalStateException;
}
