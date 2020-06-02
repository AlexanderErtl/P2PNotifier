package com.example.androidclient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.androidclient.scanner.ScannerActivity;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private EditText ip;
    private EditText port;
    private boolean ipValid = false;
    private  boolean portValid = false;

    private final int LAUNCH_SECOND_ACTIVITY = 1;
    private final int SCAN_REQUEST = 2;
    private static final int ZXING_CAMERA_PERMISSION = 3;

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
                if(text == null || text.equals("")) {
                    tv.setError("Input missing.");
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
                if(text == null || text.equals("")) {
                    tv.setError("Input missing.");
                    ipValid = false;
                } else {
                    portValid = true;
                }

            }
        });

        Button startSession = findViewById((R.id.button_startSession));
        startSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSessionActivity();
            }
        });

        Button scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openScanActivity();
            }
        });
    }

    public void openSessionActivity() {
/*
        if(!ipValid) {
            ip.setError("Input missing.");
            return;
        }else if(!portValid) {
            port.setError("Input missing.");
            return;
        }*/

        Intent intent = new Intent(this, SessionActivity.class);
        intent.putExtra("ip", ip.getText().toString());
        intent.putExtra("port", port.getText().toString());
        startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
    }

    public void openScanActivity() {
        System.out.println("Scanning");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        }
        else {
            Intent intent = new Intent(this, ScannerActivity.class);
            startActivityForResult(intent, SCAN_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == -1) {
                //String result=data.getStringExtra("result");
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, "Connection failed", Snackbar.LENGTH_LONG).show();
            } else {
                System.out.println("VERY GOOD");
            }
        }
        else if (requestCode == SCAN_REQUEST) {
            if (resultCode == RESULT_OK) {
                System.out.println(data.getDataString());
                Toast.makeText(this, data.getDataString(), Toast.LENGTH_LONG).show();
            }
            else {
                System.out.println("Scan failed");
                Toast.makeText(this, "Scan failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        if (requestCode == ZXING_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, ScannerActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
