package com.example.androidclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private EditText ip;
    private EditText port;
    private Button startSession;
    private boolean ipValid = false;
    private  boolean portValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ip = (EditText)findViewById(R.id.text_ipAddress);
        ip.addTextChangedListener(new InputValidator(ip) {
            @Override
            public void validate(String text, TextView tv) {
                // Does not handle the cases where ip "fields" are > 255
                if(text == null || text.equals("")) {
                    tv.setError(Utils.INVALID_INPUT);
                    ipValid = false;
                } else {
                    ipValid = true;
                }
            }
        });

        port = (EditText)findViewById(R.id.text_portNum);
        port.addTextChangedListener(new InputValidator(port) {
            @Override
            public void validate(String text, TextView tv) {
                int p = Integer.parseInt(text);

                if("".equals(text) || p > Utils.MAX_PORT_VAL) {
                    tv.setError(Utils.INVALID_INPUT);
                    portValid = false;
                } else {
                    portValid = true;
                }

            }
        });

        startSession = (Button)findViewById((R.id.button_startSession));
        startSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSessionActivity();
            }
        });
    }

    public void openSessionActivity() {
        if(!ipValid) {
            ip.setError(Utils.INVALID_INPUT);
            return;
        }else if(!portValid) {
            port.setError(Utils.INVALID_INPUT);
            return;
        }

        Intent intent = new Intent(this, SessionActivity.class);
        intent.putExtra(Utils.INTENT_IP, ip.getText().toString());
        intent.putExtra(Utils.INTENT_PORT, port.getText().toString());
        startActivityForResult(intent, Utils.LAUNCH_SECOND_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == -1) {
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, Utils.CONNECTION_FAILED, Snackbar.LENGTH_LONG).show();
            }
        }
    }


}
