package com.bupt.deck.websocket;

import android.content.Context;
import android.util.Log;

import com.bupt.deck.data.GlobalData;
import com.bupt.deck.db.WSConnDB;
import com.bupt.deck.db.WSConnEvent;
import com.bupt.deck.metrics.MetricsHelper;
import com.bupt.deck.utils.UUIDHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketConn {
    private static WebSocketConn mInstance = null;
    // ref: https://github.com/McLeroy/WebsocketLiveDataSample/blob/master/app/src/main/java/com/mcleroy/wesocketslivedatasample/ui/common/SocketLiveData.java#L29
    private static AtomicBoolean disconnected = new AtomicBoolean(true);
    private static String gatewayURL;
    String TAG = "WebSocketConn-Deck";
    // Application context
    private Context context;
    private WebSocket wsConnection;
    private OkHttpClient client;

    private WebSocketConn(Context context) {
        this.context = context.getApplicationContext();
        Log.i(TAG, "WebSocketConn: private constructor is called");
        connect();
        // Do not send REPORT message after built
        // String msg = DeviceStatusReporter.constructReportMsg(this.context);
        // wsConnection.send(msg);
    }

    public static void init(String url) {
        gatewayURL = url;
    }

    public static synchronized WebSocketConn getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new WebSocketConn(context);
        }
        return mInstance;
    }

    private static String constructPublicConnectURL(Context context) {
        return "http://" + gatewayURL + "/websocket?" +
                UUIDHelper.getInstance(context).getUniqueID();
    }

    public WebSocket getWsConnection() {
        return wsConnection;
    }

    public boolean isDisconnected() {
        return disconnected.get();
    }

    public void disconnect() {
        if (wsConnection != null) {
            wsConnection.close(1000, "Proactively shutdown ws connection");
            WSConnDB.getInstance(context).wsConnEventDao().insert(
                    new WSConnEvent(WSConnEvent.DISCONNECT, "Disconnect manually", context)
            );
        }
    }


    public synchronized void connect() {
        if (disconnected.compareAndSet(true, false)) {
            if (this.client == null) {
                this.client = new OkHttpClient()
                        .newBuilder()
                        .retryOnConnectionFailure(true)
                        .pingInterval(20, TimeUnit.SECONDS)
                        .build();
            }
            // Close and remove all idle connection
            this.client.connectionPool().evictAll();
            Request req = new Request.Builder()
                    .url(constructPublicConnectURL(this.context))
                    .build();
            this.wsConnection = client.newWebSocket(req, new DeckWebSocketListener(this.context));
            Log.i(TAG, "connect: WebSocket connecting ...");
            GlobalData.executorService.submit(() -> WSConnDB.getInstance(context).wsConnEventDao().insert(
                    new WSConnEvent(WSConnEvent.CONNECT, context)
            ));
        } else {
            Log.w(TAG, "connect: WebSocket connection has been established, ignore");
        }
    }

    private static class DeckWebSocketListener extends WebSocketListener {
        String TAG = "MyWebSocketListener-Deck";

        private Context context;

        // This absolutely ApplicationContext
        public DeckWebSocketListener(Context context) {
            this.context = context;
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);
            Log.i(TAG, "onOpen: A new WebSocket connection has been established");
            disconnected.set(false);
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            disconnected.set(true);
            // If this connection is down, re-connect to server
            int code = response != null ? response.code() : 400;
            String message = response != null ? response.message() : t.getMessage();

            Log.i(TAG, String.format("On Failure: Code: %s, message: %s", code, message));
            GlobalData.executorService.submit(() -> WSConnDB.getInstance(context).wsConnEventDao().insert(
                    new WSConnEvent(WSConnEvent.FAILURE, String.format("Code: %s, message: %s", code, message), context)
            ));

            // TODO: When will message be null
            // ref: https://github.com/McLeroy/WebsocketLiveDataSample/blob/master/app/src/main/java/com/mcleroy/wesocketslivedatasample/ui/common/SocketLiveData.java#L101
            if (code == 400) {
                Log.i(TAG, "onFailure: schedule reconnect work");
                WSReconnectionWorker.schedule(this.context);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            long receivedMsgTs = System.currentTimeMillis();
            super.onMessage(webSocket, text);
            try {
                Log.i(TAG, "onMessage: Get message bytes: " + text.getBytes().length +
                        " at " + System.currentTimeMillis());
                // MsgHandler will return a string in format: {msgtype}_{taskid}
                String msgTaskIDKey = MsgHandler.handle(text, this.context);
                // METRICS: save message type, taskID and timestamp when receiving message from
                // gateway to MetricsPreference
                if (msgTaskIDKey != null) {
                    String metricsKey = "Receive-msg-" + msgTaskIDKey;
                    MetricsHelper.put(this.context, metricsKey, receivedMsgTs);
                }
            } catch (Exception e) {
                Log.e(TAG, "onMessage: convert json to json object error, content: " + e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosing(webSocket, code, reason);
            GlobalData.executorService.submit(() -> WSConnDB.getInstance(context).wsConnEventDao().insert(
                    new WSConnEvent(WSConnEvent.CLOSING, String.format("code: %s, reason: %s", code, reason), context)
            ));
            Log.i(TAG, "onClosing: Remote server indicates that no more message will be transmit");
            disconnected.set(true);
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
            GlobalData.executorService.submit(() -> WSConnDB.getInstance(context).wsConnEventDao().insert(
                    new WSConnEvent(WSConnEvent.CLOSED, String.format("code: %s, reason: %s", code, reason), context)
            ));
            Log.i(TAG, "onClosed: WebSocket connection has closed");
            disconnected.set(true);
        }
    }
}

