package edge.droid.server.data;

public enum MsgType {

    DEX_FILE("DEXFILE", "tran dex file"),
    DEX_FILE_ACK("DEXFILEACK", "dex file ack response"),
    PARAMS("", ""),
    REQ_RESULT("", ""),
    CONNECT("", ""),
    REPORT("REPORT", "device info"),
    RESULT("RESULT", "get task result"),
    REQ_DEX_FILE("", ""),
    REQ_PARAMS("", ""),
    METRICS("METRICS", "time log"),
    PING("PING", "heart request"),
    PONG("PONG", "heart response")
    ;

    private String msgTypeStr;
    private String description;

    private MsgType(String msgTypeStr, String description) {
        this.msgTypeStr = msgTypeStr;
        this.description = description;
    }

    public String getMsgTypeStr() {
        return this.msgTypeStr;
    }

    public String getDescription() {
        return this.description;
    }

    public static MsgType getMsgTypeByStr(String msgTypeStr){
        for(MsgType msgType : MsgType.values()){
            if(null != msgTypeStr && msgTypeStr.equals(msgType.getMsgTypeStr())){
                return msgType;
            }
        }
        return null;
    }
}
