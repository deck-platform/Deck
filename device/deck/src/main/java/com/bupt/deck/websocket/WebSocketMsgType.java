package com.bupt.deck.websocket;

public enum WebSocketMsgType {

    DEXFILE("DEXFILE"),
    DEXFILE_ACK("DEXFILEACK"),
    CANCEL("CANCEL"),
    PARAMS("PARAMS"),
    REPORT("REPORT"),
    RESULT("RESULT"),
    METRICS("METRICS"),
    PING("PING"),
    PONG("PONG"),
    REQ_PARAMS("REQPARAMS"),
    REQ_DEXFILE("REQDEXFILE"),
    REQ_RESULT("REQRESULT");

    private String msgTypeName;

    WebSocketMsgType(String msgTypeStr) {
        this.msgTypeName = msgTypeStr;
    }

    public String getMsgTypeName() {
        return this.msgTypeName;
    }
}
