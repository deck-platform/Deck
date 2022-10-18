package edge.droid.server.wrapper;

import edge.droid.server.data.MsgType;
import edge.droid.server.data.Source;
import edge.droid.server.model.FileModel;
import edge.droid.server.model.WebSocketMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WebSocketMsgWrapper {

    public WebSocketMessage webSocketMessage(String uuID, MsgType msgType, String taskID, String dexFileContent, List<String> libFileContentList, int times, List<FileModel> fileModelList, Map<Source, List<String>> permissionInfo) {
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setUuid(uuID);
        webSocketMessage.setMsgType(msgType.getMsgTypeStr());
        webSocketMessage.setTaskID(taskID);
        webSocketMessage.setParams("");
        webSocketMessage.setDexFileContent(dexFileContent);
        webSocketMessage.setTs(System.currentTimeMillis());
        webSocketMessage.setLibFileContentList(libFileContentList);
        webSocketMessage.setTimes(times);
        webSocketMessage.setFileModelList(fileModelList);
        webSocketMessage.setPermissionInfo(permissionInfo);
        return webSocketMessage;
    }
}
