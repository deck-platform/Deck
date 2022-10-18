package edge.droid.server.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class FileModel {

    @JSONField(name = "filename")
    private String fileName;
    @JSONField(name = "content")
    private String content;

    public FileModel(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }
}