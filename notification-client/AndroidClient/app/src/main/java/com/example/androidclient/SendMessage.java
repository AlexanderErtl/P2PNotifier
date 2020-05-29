package com.example.androidclient;

import android.os.AsyncTask;

import java.io.PrintWriter;

class SendMessage extends AsyncTask<String, Void, String> {

    private PrintWriter pw;
    private MyAdapter myAdapter;

    public SendMessage(PrintWriter pw, MyAdapter myAdapter) {
        super();
        this.pw = pw;
        this.myAdapter = myAdapter;
    }

    protected String doInBackground(String... strings) {
        String message = strings[0];
        pw.println (message);
        pw.flush();
        System.out.println("SENT");
        return message;
    }

    @Override
    protected void onPostExecute(String message) {
        myAdapter.messages.add(new Message(message, MessageType.MESSAGE_SENT));
        myAdapter.notifyItemInserted(myAdapter.messages.size());
    }
}