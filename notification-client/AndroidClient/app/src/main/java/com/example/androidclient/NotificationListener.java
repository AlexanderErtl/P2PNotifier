package com.example.androidclient;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


@SuppressLint({"OverrideAbstract", "NewApi"})
public class NotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if(notification != null) {
            Bundle extras = notification.extras;
            String message = "";
            // Create json with title, text...
            if(extras.getString(Notification.EXTRA_TEXT) != null)
            {
                message = message.concat(extras.getString(Notification.EXTRA_TEXT));
            }

            Intent intent = new Intent();
            intent.setAction(Utils.INTENT_ACTION_SEND_MESSAGE);
            intent.putExtra(Utils.INTENT_MESSAGE, message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
