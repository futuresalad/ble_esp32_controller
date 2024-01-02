package com.example.haendchen;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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

        BLE ble = BLE.getInstance(getApplicationContext());

        BLE.getInstance(this).setBluetoothConnectionListener(this);

        ProgressBar spinning_wheel = (ProgressBar) findViewById(R.id.spinning_wheel);
        TextView    scan_text = (TextView)findViewById(R.id.scan_text);

        ble.startBleScan();

        spinning_wheel.setVisibility(View.VISIBLE);

        final Runnable r = new Runnable() {
            public void run() {
                spinning_wheel.setVisibility(View.GONE);
                scan_text.setText("Device not found");
                handler.postDelayed(this, 5000);
            }
        };


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

