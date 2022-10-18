package edge.droid.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.websocket.Session;

@Data
public class DeviceInfo {

    private String uuID;
    private String connectTime;
    private Long connectTimestamp;
    @JsonIgnore
    private Session session;
}
