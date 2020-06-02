package com.example.androidclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidclient.ssl.MyPSKKeyManager;

import java.io.IOException;
import java.util.ArrayList;

import org.conscrypt.*;

import javax.crypto.ExemptionMechanismException;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class SessionActivity extends AppCompatActivity {

    private Button send;
    private EditText messageInput;
    Thread clientThread;
    ClientThread c;
    RecyclerView rv;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        getSupportActionBar().setTitle("Session");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String ipAddress = getIntent().getStringExtra("ip");
        String port = getIntent().getStringExtra("port");

        ArrayList<Message> messages = new ArrayList<>();

        rv = (RecyclerView)findViewById(R.id.recycleView);
        final MyAdapter myAdapter = new MyAdapter(this, messages);
        rv.setAdapter(myAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));


        send = (Button)findViewById(R.id.button_send);
        messageInput = (EditText)findViewById(R.id.text_input);

        progress = new ProgressDialog(this);
        progress.setMessage("Connecting");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
        //c = new ClientThread(ipAddress, Integer.parseInt(port), myAdapter, this);
        c = new ClientThread("10.0.2.2", 4433, myAdapter, this);
        clientThread = new Thread(c);
        clientThread.start();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString();
                if(message.equals("")) {
                    return;
                }
                SendMessage s = new SendMessage(c.out, myAdapter);
                s.execute(message);
                messageInput.getText().clear();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        System.out.println("BACK PRESSED");

        c.closeSocket();
        //clientThread.interrupt();
        //NavUtils.navigateUpFromSameTask(this);
        finish();
    }

    public void endActivity() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("done", -1);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}