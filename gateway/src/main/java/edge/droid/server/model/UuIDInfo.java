package edge.droid.server.model;

import lombok.Data;

@Data
public class UuIDInfo {

    private int receiveTimes;
    private long lastReceiveTime;

    public UuIDInfo(int receiveTimes, long lastReceiveTime) {
        this.receiveTimes = receiveTimes;
        this.lastReceiveTime = lastReceiveTime;
    }
}
