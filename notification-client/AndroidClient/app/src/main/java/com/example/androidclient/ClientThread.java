package com.example.androidclient;

import android.os.Handler;
import android.os.Looper;

import com.example.androidclient.ssl.MyPSKKeyManager;

import org.conscrypt.PSKKeyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class ClientThread implements Runnable {

    private SSLSocket clientSocket;
    PrintWriter out;
    private BufferedReader in;
    private String ipAddress;
    private int port;
    private MyAdapter adapter;
    private SessionActivity sa;

    ClientThread(String ip, int port, MyAdapter adapter, SessionActivity sa) {
        this.ipAddress = ip;
        this.port = port;
        this.adapter = adapter;
        this.sa = sa;
    }

    @Override
    public void run() {
        Handler handler = new Handler(Looper.getMainLooper());
        try {

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
            clientSocket.connect(new InetSocketAddress(ipAddress,  port), 2000);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    sa.progress.cancel();
                }
            });

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("FAILED TO CREATE A SOCKET.");
            System.out.println(e.getMessage());
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    sa.progress.cancel();
                    sa.endActivity();
                }
            });
            return;
        }

        while(!Thread.currentThread().isInterrupted()) {
            String response = null;
            try {
                response = in.readLine();
                adapter.messages.add(new Message(response, MessageType.MESSAGE_RECEIVED));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemInserted(adapter.messages.size());
                    }
                });

                //System.out.println(response);
            } catch (IOException e) {
                System.out.println("Socket closed.");
                return;
            }
        }

        System.out.println("Thread stopped.");
    }

    void closeSocket() {
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            System.out.println("SOCKET CLOSED");
        }
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
