package com.bupt.deck.devicestatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.bupt.deck.utils.CommonHelper;
import com.bupt.deck.websocket.WebSocketConn;

// ref: https://gist.github.com/ishitcno1/7261765
public class ScreenStateReceiver extends BroadcastReceiver {
    String TAG = "ScreenState-Deck";

    Class serviceClass;

    public ScreenStateReceiver(Class serviceClass) {
        this.serviceClass = serviceClass;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            Log.i(TAG, "onReceive: screen is on");

            // Check and restart WS connection
            if (WebSocketConn.getInstance(context).isDisconnected()) {
                Log.i(TAG, "onReceive: ws connection is disconnected when screen on, reconnect");
                WebSocketConn.getInstance(context).connect();
            } else {
                Log.i(TAG, "onReceive: ws connection is still connected when screen on, ignore");
            }

            // Start Deck Service
            if (!CommonHelper.isServiceRunning(serviceClass, context.getApplicationContext())) {
                Log.i(TAG, "onReceive: restart DeckService");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, serviceClass));
                } else {
                    context.startService(new Intent(context, serviceClass));
                }
            } else {
                Log.w(TAG, "onReceive: DeckService is still running, ignore start command");
            }

        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            Log.i(TAG, "onReceive: screen is off");
        }
    }
}
