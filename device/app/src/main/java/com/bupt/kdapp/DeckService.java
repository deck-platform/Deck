package com.bupt.kdapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bupt.deck.BuildConfig;
import com.bupt.deck.devicestatus.ScreenStateReceiver;
import com.bupt.deck.utils.UUIDHelper;
import com.bupt.deck.websocket.WSConnectionChecker;
import com.bupt.deck.websocket.WSHeartBeatWorker;
import com.bupt.deck.websocket.WebSocketConn;

public class DeckService extends Service {

    String TAG = "Deck-Service-Deck";

    private ScreenStateReceiver screenStateReceiver;

    private String url = BuildConfig.serverUrl;

    // We want to run this service infinitely rather than binding to any components, so return null.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: Start Deck Service");

        // Create notification channel
        NotificationChannel channel;
        Notification notification;
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(), 0, activityIntent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("1",
                    "NotificationChannel",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            notification = new NotificationCompat.Builder(this, "1")
                    .setContentTitle("KD Service")
                    .setContentText(UUIDHelper.getInstance(getApplicationContext()).getUniqueID().substring(0, 8))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build();
        } else {
            notification = new Notification.Builder(this)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setTicker("KD Service is starting")
                    .setContentTitle("KD Service")
                    .setContentText(UUIDHelper.getInstance(getApplicationContext()).getUniqueID().substring(0, 8))
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .build();
        }

        // Start foreground service
        startForeground(1, notification);

        // Start ScreenStateReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateReceiver = new ScreenStateReceiver(DeckService.class);
        registerReceiver(screenStateReceiver, intentFilter);

        // Cancel previous workers if exists
        // WSConnectionChecker.cancel();
        // NetworkChecker.cancel();
        // WSHeartBeatWorker.cancel();


        WebSocketConn.init(url);
        WebSocketConn.getInstance(getApplicationContext()).connect();

        // Start periodic checker for WebSocket connection
        WSConnectionChecker.schedule(getApplicationContext());
        // Start heart beat worker for WebSocket connection
        WSHeartBeatWorker.schedule(getApplicationContext());

        // ref: https://llin233.github.io/2015/11/16/How-to-prevent-service/
        flags = START_STICKY;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO: Should clean up any resources
        Log.i(TAG, "onDestroy: Deck Service has been destroyed");
        if (screenStateReceiver != null) {
            unregisterReceiver(screenStateReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
