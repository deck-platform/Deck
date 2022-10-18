package edge.droid.server.model;

import lombok.Data;

@Data
public class SendErrorModel {

    private String uuID;
    private String reason;
    private long timestamp;

    public SendErrorModel(String uuID, String reason, long timestamp) {
        this.uuID = uuID;
        this.reason = reason;
        this.timestamp = timestamp;
    }
}
