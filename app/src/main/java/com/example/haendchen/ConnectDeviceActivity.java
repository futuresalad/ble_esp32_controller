package com.example.haendchen;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

public class ConnectDeviceActivity extends AppCompatActivity implements BluetoothConnectionListener {
    private final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connect_device);

        // Getting the device name from the QR code scan
        String deviceName = getIntent().getStringExtra("QR_CODE_CONTENT");
        if (deviceName == null) {
            finish();
        }

        // BLE instance with application context
        BLE ble = BLE.getInstance(getApplicationContext());
        BLE.getInstance(this).setBluetoothConnectionListener(this);

        // Show spinning wheel while no device is connected yet
        ProgressBar spinningWheel = (ProgressBar) findViewById(R.id.spinning_wheel);
        TextView    scanText = (TextView)findViewById(R.id.scan_text);
        Button      btnRetry = findViewById(R.id.retry_btn);

        ble.startBleScan(deviceName);
        spinningWheel.setVisibility(View.VISIBLE);

        // Retry scan if nothing is found
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent retryIntent = new Intent(ConnectDeviceActivity.this, ConnectDeviceActivity.class);
                startActivity(retryIntent);
            };
        });

        // Show retry button and remove spinning wheel if device is not found
        handler.postDelayed(new Runnable() {
            public void run() {
                // Checking if the device was found
                if (!ble.isConnected()) {
                    spinningWheel.setVisibility(View.GONE);
                    scanText.setText("Device not found");
                    btnRetry.setVisibility(View.VISIBLE);
                }
            }
        }, 10000);

        // Callback for back button
        OnBackPressedCallback callbackConnectBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();

            }};

        getOnBackPressedDispatcher().addCallback(this, callbackConnectBack);
    }

    @Override
    protected void onResume(){

        super.onResume();

        BLE ble = BLE.getInstance(getApplicationContext());

        if (ble.isConnected()) {
            Intent onConnectionChangeIntent = new Intent(this, MainActivity.class);
            Log.d("MyApp", "Device already connected! Opening Menu");
            onConnectionChangeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(onConnectionChangeIntent);
        }
    }

    // Communicate connection change through interface
    @Override
    public void onConnectionStateChanged(boolean isConnected) {

            Intent onConnectionChangeIntent = new Intent(this, MainActivity.class);
            Log.d("MyApp", "Device connected or disconnected! Opening Menu");
            onConnectionChangeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(onConnectionChangeIntent);
    }

    @Override
    public void onDataReceive(int rxData) {
        Log.d("MyApp", "Data recieved: "+ rxData);
    }
}

