package edge.droid.server.model;

import lombok.Data;

@Data
public class DeviceResponse {

    private String uuID;
    private Long timestamp;

    public DeviceResponse(String uuID, Long timestamp) {
        this.timestamp = timestamp;
        this.uuID = uuID;
    }
}
