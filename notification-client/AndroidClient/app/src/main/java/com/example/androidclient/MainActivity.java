package com.example.androidclient;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.androidclient.scanner.ScannerActivity;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private EditText ip;
    private EditText port;
    Button startSession;
    Button scanButton;
    ProgressDialog progress;
    private boolean ipValid = false;
    private  boolean portValid = false;

    private final int LAUNCH_SECOND_ACTIVITY = 1;
    private final int SCAN_REQUEST = 2;
    private final int NOTIFICATION_LISTENER_REQUEST = 5;
    private static final int ZXING_CAMERA_PERMISSION = 3;
    private static final String CONNECTION_STATE = "connection state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.INTENT_ACTION_MESSAGE_RECEIVED);
        intentFilter.addAction(Utils.INTENT_ACTION_SOCKET_STATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

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


        startSession = findViewById((R.id.button_startSession));
        startSession.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if("Disconnect".equals(startSession.getText()))
                {
                    startSession.setText("Connect");
                    enableMainActivityElements(true);
                    stopService(new Intent(v.getContext(), ReceiveMessageService.class));
                } else {
                    boolean hasAccess = checkNotificationListenerPermission();
                    if (!hasAccess) {
                        startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), NOTIFICATION_LISTENER_REQUEST);
                    } else {
                        startNotifier();
                    }
                }
            }
        });

        scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openScanActivity();
            }
        });
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if (requestCode == SCAN_REQUEST) {
            if (resultCode == RESULT_OK) {
                System.out.println(data.getDataString());
                Toast.makeText(this, data.getDataString(), Toast.LENGTH_LONG).show();
                // Only for demonstration purposes, e.g. IP:192.168.1.105, Port:7878
                String serverData = data.getDataString();
                String[] ipPort = serverData.split(",");
                if(ipPort.length != 2) {
                    System.out.println("Invalid QR code.");
                } else {
                    String[] ip = ipPort[0].split(":");
                    String[] port = ipPort[1].split(":");

                    if(ip.length != 2 || port.length != 2) {
                        System.out.println("Invaild QR code.");
                    } else {
                        this.ip.setText(ip[1]);
                        this.port.setText(port[1]);
                        System.out.println("IP: " + ip[1]);
                        System.out.println("PORT: " + port[1]);
                    }
                }
            }
            else {
                System.out.println("Scan failed");
                Toast.makeText(this, "Scan failed", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == NOTIFICATION_LISTENER_REQUEST) {
            boolean hasAccess = checkNotificationListenerPermission();
            if(!hasAccess) {
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, Utils.PERMISSION_DENIED, Snackbar.LENGTH_LONG).show();
            } else {
                startNotifier();
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

    private boolean checkNotificationListenerPermission() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startNotifier() {
        progress = new ProgressDialog(this);
        progress.setMessage("Connecting");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        Intent serviceIntent = new Intent(this, ReceiveMessageService.class);
        serviceIntent.putExtra(Utils.INTENT_IP, "192.168.1.106");
        serviceIntent.putExtra(Utils.INTENT_PORT, Integer.toString(4433));
        startForegroundService(serviceIntent);

        startSession.setText("Disconnect");
        enableMainActivityElements(false);



    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Utils.INTENT_MESSAGE);
            if(Utils.SOCKET_CONNECTED.equals(message)) {
                progress.cancel();
            } else if(Utils.SOCKET_DISCONNECTED.equals(message)) {
                progress.cancel();
                View parentLayout = findViewById(android.R.id.content);
                Snackbar.make(parentLayout, Utils.CONNECTION_FAILED, Snackbar.LENGTH_LONG).show();
                startSession.setText("Connect");
                enableMainActivityElements(true);
            }
        }
    };


    private void enableMainActivityElements(boolean state) {
        ip.setEnabled(state);
        ip.setFocusable(state);
        port.setEnabled(state);
        port.setFocusable(state);
        scanButton.setEnabled(state);
        scanButton.setFocusable(state);
    }
}
