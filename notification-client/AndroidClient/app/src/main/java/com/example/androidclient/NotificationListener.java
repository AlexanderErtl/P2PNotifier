package com.example.androidclient;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;


@SuppressLint({"OverrideAbstract", "NewApi"})
public class NotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if(notification != null) {
            Bundle extras = notification.extras;
            String message = "";
            JSONObject json = new JSONObject();

            // Create json with title, text...
            try {
                json.put("title", extras.getString(Notification.EXTRA_TITLE));
                json.put("text" ,extras.getString(Notification.EXTRA_TEXT));
                json.put("info_text" ,extras.getString(Notification.EXTRA_INFO_TEXT));
                json.put("messaging_person" ,extras.getString(Notification.EXTRA_MESSAGING_PERSON));
            } catch (JSONException e) {
                System.out.println("Json went wrong.");
                e.printStackTrace();
            }

            message = json.toString();

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
