package com.example.androidclient;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.androidclient.ssl.MyPSKKeyManager;

import org.conscrypt.PSKKeyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import static com.example.androidclient.App.CHANNEL_MESSAGES;
import static com.example.androidclient.App.CHANNEL_PERSISTENT;

public class ReceiveMessageService extends IntentService {


    private SSLSocket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String ipAddress;
    private int port;
    private NotificationManagerCompat nm;

    public class SendMessage implements Runnable {
        private String m;
        public SendMessage(String message) {
            this.m = message;
        }

        @Override
        public void run() {
            out.println(m);
            out.flush();
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Utils.INTENT_MESSAGE);
            Thread th = new Thread(new SendMessage(message));
            th.start();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.INTENT_ACTION_SEND_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        nm = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_PERSISTENT)
                    .setContentTitle("Android client")
                    .setContentText("Running...")
                    .setSmallIcon(R.drawable.ic_connected)
                    .build();
            startForeground(1, notification);
        }
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    public ReceiveMessageService() {
        super("IntentService");
        setIntentRedelivery(true);
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            //handle this
            ipAddress = intent.getStringExtra(Utils.INTENT_IP);
            port = Integer.parseInt(intent.getStringExtra(Utils.INTENT_PORT));

            SSLSocketFactory sslSocketFactory = initSocketFactory();

            System.out.println("Got socketFactory");
            try {
                clientSocket = (SSLSocket) sslSocketFactory.createSocket();
            } catch (IOException|NullPointerException e) {
                System.out.println("Error creating socket");
                System.out.println(e.getMessage());
                return;
            }
            System.out.println("connecting to: " + ipAddress + ":" + port);
            clientSocket.connect(new InetSocketAddress(ipAddress,  port), 4000);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        } catch (Exception e) {
            System.out.println("Failed to connect");
            Intent intentSocketState = new Intent();
            intentSocketState.setAction(Utils.INTENT_ACTION_SOCKET_STATE);
            intentSocketState.putExtra(Utils.INTENT_MESSAGE, Utils.SOCKET_DISCONNECTED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentSocketState);
            return;
        }

        Intent intentSocketState = new Intent();
        intentSocketState.setAction(Utils.INTENT_ACTION_SOCKET_STATE);
        intentSocketState.putExtra(Utils.INTENT_MESSAGE, Utils.SOCKET_CONNECTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSocketState);

        String response = null;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                response = in.readLine();
                Intent notificationIntent = new Intent(this, SessionActivity.class);
                notificationIntent.putExtra(Utils.INTENT_MESSAGE, response);
                PendingIntent pendingIntent = PendingIntent.getActivity(this,
                        0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification n = new NotificationCompat.Builder(this, CHANNEL_MESSAGES)
                        .setSmallIcon(R.drawable.ic_message)
                        .setContentTitle("New message")
                        .setContentText(response)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setAutoCancel(true)
                        .build();
                nm.notify(2, n);

                Intent intentMessage = new Intent();
                intentMessage.setAction(Utils.INTENT_ACTION_MESSAGE_RECEIVED);
                intentMessage.putExtra(Utils.INTENT_MESSAGE, response);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentMessage);

            } catch (IOException e) {
                System.out.println("Socket closed.");
                return;
            }
        }
    }


    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        try {
            clientSocket.close();
        } catch (IOException e) {
        }
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private SSLSocketFactory initSocketFactory() {
        try {
            PSKKeyManager pskKeyManager = new MyPSKKeyManager();
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(new KeyManager[] {pskKeyManager}, new TrustManager[0], null);
            System.out.println("initialized sslContext");
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            //SSLSocket sslSocket = (SSLSocket)socketFactory.createSocket();
            return socketFactory;
        }
        catch (Exception e) {
            System.out.println("Error initializing socket. ");
            System.out.println(e.getMessage());
        }
        return null;
    }

}
