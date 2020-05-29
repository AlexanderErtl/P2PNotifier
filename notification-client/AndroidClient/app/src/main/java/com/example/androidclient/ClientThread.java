package com.example.androidclient;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientThread implements Runnable {

    private Socket clientSocket;
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
            clientSocket = new Socket();
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
            //System.out.println("FAILED TO CREATE A SOCKET.");
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
}
