package com.example.androidclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SessionActivity extends AppCompatActivity {

    private Button send;
    private EditText messageInput;
    RecyclerView rv;
    ProgressDialog progress;
    ArrayList<Message> messages = new ArrayList<>();
    final MyAdapter myAdapter = new MyAdapter(this, messages);


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Utils.INTENT_MESSAGE);
            if(Utils.SOCKET_CONNECTED.equals(message)) {
                progress.cancel();
            } else if(Utils.SOCKET_DISCONNECTED.equals(message)) {
                progress.cancel();
                endActivity();
            } else {
                System.out.println(intent.getAction());
                myAdapter.messages.add(new Message(message, Utils.MESSAGE_TYPE_RECEIVED));
                myAdapter.notifyItemInserted(myAdapter.messages.size());
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.INTENT_ACTION_MESSAGE_RECEIVED);
        intentFilter.addAction(Utils.INTENT_ACTION_SOCKET_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        boolean freshStart = true;
        String messageAfterResume = getIntent().getStringExtra(Utils.INTENT_MESSAGE);
        if(messageAfterResume != null) {
            myAdapter.messages.add(new Message(messageAfterResume, Utils.MESSAGE_TYPE_RECEIVED));
            myAdapter.notifyItemInserted(myAdapter.messages.size());
            freshStart = false;
        }

        setContentView(R.layout.activity_session);
        getSupportActionBar().setTitle("Session");

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeButtonEnabled(true);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.custom_back_button, null);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        TextView backButton = (TextView) mCustomView.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        String ipAddress = getIntent().getStringExtra(Utils.INTENT_IP);
        String port = getIntent().getStringExtra(Utils.INTENT_PORT);


        rv = (RecyclerView)findViewById(R.id.recycleView);
        rv.setAdapter(myAdapter);
        rv.setLayoutManager(new LinearLayoutManager(this));


        send = (Button)findViewById(R.id.button_send);
        messageInput = (EditText)findViewById(R.id.text_input);

        if(freshStart) {
            progress = new ProgressDialog(this);
            progress.setMessage("Connecting");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();
        }

        Intent serviceIntent = new Intent(this, ReceiveMessageService.class);
        //serviceIntent.putExtra(Utils.INTENT_IP, "192.168.1.106");
        serviceIntent.putExtra(Utils.INTENT_IP, "10.0.2.2");
        serviceIntent.putExtra(Utils.INTENT_PORT, Integer.toString(4433));
        startForegroundService(serviceIntent);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString();
                if("".equals(message)) {
                    return;
                }
                Intent intent1 = new Intent();
                intent1.setAction(Utils.INTENT_ACTION_SEND_MESSAGE);
                intent1.putExtra(Utils.INTENT_MESSAGE, message);
                LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent1);
                myAdapter.messages.add(new Message(message, Utils.MESSAGE_TYPE_SENT));
                myAdapter.notifyItemInserted(myAdapter.messages.size());
                messageInput.getText().clear();

            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed()
    {
        Intent serviceIntent = new Intent(this, ReceiveMessageService.class);
        stopService(serviceIntent);
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


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}

//TODO: Connect -> Destroy activity -> Receive notification -> press Disconnect
// - currently: Closes the app
// - how it should be: returns to the main activity (this works if session activity not destroyed)

//TODO: Connect -> Destroy activity -> Start the app while receive message service is running in the background
// - currently: Starts an app from main activity
// - how it should be: opens session activity directly since receive message service is already connected