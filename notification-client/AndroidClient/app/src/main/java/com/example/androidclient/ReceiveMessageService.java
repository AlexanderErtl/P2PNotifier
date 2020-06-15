package com.example.androidclient;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;

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

public class ReceiveMessageService extends Service {


    private SSLSocket clientSocket;
    private PrintWriter out = null;
    private BufferedReader in;
    private String ipAddress;
    private int port;
    private NotificationManagerCompat nm;
    private boolean success = true;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.INTENT_ACTION_SEND_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        ipAddress = intent.getStringExtra(Utils.INTENT_IP);
        String portString = intent.getStringExtra(Utils.INTENT_PORT);
        try {
            port = Integer.parseInt(portString);
        } catch (Exception e) {
            System.out.println("Could not parse port '" +  portString + "'");
            port = 0;
        }

        nm = NotificationManagerCompat.from(this);
        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_PERSISTENT)
                    .setContentTitle("Android client")
                    .setContentText("Running...")
                    .setSmallIcon(R.drawable.ic_connected)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
            startForeground(1, notification);
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                connectToServer();
            }
        };
        Executor connect = new Executor(r);
        connect.execute();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void connectToServer() {
        try {
            //handle this

            SSLSocketFactory sslSocketFactory = initSocketFactory();

            System.out.println("Got socketFactory");
            try {
                clientSocket = (SSLSocket) sslSocketFactory.createSocket();
            } catch (IOException|NullPointerException e) {
                System.out.println("Error creating socket");
                System.out.println(e.getMessage());
                success = false;
            }
            System.out.println("connecting to: " + ipAddress + ":" + port);
            clientSocket.connect(new InetSocketAddress(ipAddress,  port), 2000);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        } catch (Exception e) {
            System.out.println("Failed to connect");
            Intent intentSocketState = new Intent();
            intentSocketState.setAction(Utils.INTENT_ACTION_SOCKET_STATE);
            intentSocketState.putExtra(Utils.INTENT_MESSAGE, Utils.SOCKET_DISCONNECTED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intentSocketState);
            success = false;
        }

        Intent intentSocketState = new Intent();
        intentSocketState.setAction(Utils.INTENT_ACTION_SOCKET_STATE);
        intentSocketState.putExtra(Utils.INTENT_MESSAGE, Utils.SOCKET_CONNECTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentSocketState);
    }

    public class SendMessage implements Runnable {
        private String m;
        public SendMessage(String message) {
            this.m = message;
        }

        @Override
        public void run() {
            if(m != null) {
                out.println(m);
                out.flush();
            }
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(out == null) {
                return;
            }
            String message = intent.getStringExtra(Utils.INTENT_MESSAGE);
            Executor sendMessage = new Executor(new SendMessage(message));
            sendMessage.execute();
        }
    };


    public void onDestroy() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("socket already closed");
                }
            }
        };
        Executor closeSocket = new Executor(r);
        closeSocket.execute();

        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
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


    private class Executor extends AsyncTask<Void, Void, Void> {

        private Runnable runnable;

        public Executor(Runnable r) {
            this.runnable = r;
        }

        @Override
        protected Void doInBackground(Void... params) {

            this.runnable.run();
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            if(!success) {
                stopSelf();
            }
        }
    }


}
