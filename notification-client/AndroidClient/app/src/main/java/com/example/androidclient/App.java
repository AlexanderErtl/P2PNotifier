package com.example.androidclient;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_PERSISTENT = "clientChannel";
    public static final String CHANNEL_MESSAGES = "receiveMessageService";
    private static Context appContext;

    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel servicePersistentChannel = new NotificationChannel(CHANNEL_PERSISTENT, "Client channel", NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel serviceMessagesChannel = new NotificationChannel(CHANNEL_MESSAGES, "Receive Message Channel", NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = getSystemService(NotificationManager.class);

            if(manager == null) {
                System.exit(Utils.INIT_FAILED);
            }

            manager.createNotificationChannel(servicePersistentChannel);
            manager.createNotificationChannel(serviceMessagesChannel);
        }
    }

    public static Context getAppContext() {
        return appContext;
    }

}
