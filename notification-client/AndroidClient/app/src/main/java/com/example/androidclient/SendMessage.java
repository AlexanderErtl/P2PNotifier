package com.example.androidclient;

import android.os.AsyncTask;

import java.io.PrintWriter;

class SendMessage extends AsyncTask<String, Void, String> {

    private PrintWriter pw;
    private MyAdapter myAdapter;

    public SendMessage() {
        super();
    }

    protected String doInBackground(String... strings) {

        return null;
    }

    @Override
    protected void onPostExecute(String message) {
        myAdapter.messages.add(new Message(message, Utils.MESSAGE_TYPE_SENT));
        myAdapter.notifyItemInserted(myAdapter.messages.size());
    }
}