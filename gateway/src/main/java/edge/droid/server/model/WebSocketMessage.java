package edge.droid.server.model;

import com.alibaba.fastjson.annotation.JSONField;
import edge.droid.server.data.Source;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WebSocketMessage {

    @JSONField(name = "taskid")
    private String taskID;

    @JSONField(name = "msgtype")
    private String msgType;

    @JSONField(name = "dexfilecontent")
    private String dexFileContent;

    @JSONField(name = "libfilecontentList")
    private List<String> libFileContentList;

    @JSONField(name = "params")
    private String params;

    @JSONField(name = "result")
    private String result;

    @JSONField(name = "uuid")
    private String uuid;

    @JSONField(name = "ts")
    private long ts;

    @JSONField(name = "data")
    private String data;

    @JSONField(name = "times")
    private int times;

    @JSONField(name = "files")
    private List<FileModel> fileModelList;

    @JSONField(name = "permissionInfo")
    private Map<Source, List<String>> permissionInfo;
}
