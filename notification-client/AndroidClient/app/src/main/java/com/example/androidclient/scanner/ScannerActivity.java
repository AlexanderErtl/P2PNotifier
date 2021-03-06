package com.example.androidclient.scanner;

import com.example.androidclient.R;
import com.google.zxing.Result;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_scanner);

        ViewGroup contentFrame = findViewById(R.id.content_frame);
        scannerView = new ZXingScannerView(this);
        contentFrame.addView(scannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("Resuming: Starting camera");
        scannerView.startCamera();
        scannerView.setResultHandler(this);
    }

    @Override
    public void onPause() {
        System.out.println("Pausing: Stopping camera");
        scannerView.stopCamera();
        super.onPause();
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(rawResult.getText()));
        setResult(RESULT_OK, intent);
        finish();

        //TODO user confirmation
        /*
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scannerView.resumeCameraPreview(ScannerActivity.this);
            }
        }, 2000);*/
    }
}