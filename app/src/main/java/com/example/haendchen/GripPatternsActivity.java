package com.example.haendchen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

// GripPatternsActivity which handles different grip pattern commands sent to a BLE device
public class GripPatternsActivity extends AppCompatActivity implements BluetoothConnectionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grip_patterns);

        // Get the singleton BLE instance and set this activity as the BluetoothConnectionListener
        BLE ble = BLE.getInstance(getApplicationContext());
        BLE.getInstance(this).setBluetoothConnectionListener(this);

        // Buttons for each grip pattern
        Button btn_open = findViewById(R.id.btn_open);
        Button btn_grab = findViewById(R.id.btn_grab);
        Button btn_indicate = findViewById(R.id.btn_indicate);
        Button btn_thumbs = findViewById(R.id.btn_thumbs);
        Button btn_rock = findViewById(R.id.btn_rock);
        Button btn_pinch = findViewById(R.id.btn_pinch);

        // onClick listeners for each button to send specific grip pattern commands to the BLE device
        btn_open.setOnClickListener(v -> ble.writeCharacteristic("G_1".getBytes()));
        btn_grab.setOnClickListener(v -> ble.writeCharacteristic("G_2".getBytes()));
        btn_indicate.setOnClickListener(v -> ble.writeCharacteristic("G_3".getBytes()));
        btn_thumbs.setOnClickListener(v -> ble.writeCharacteristic("G_4".getBytes()));
        btn_rock.setOnClickListener(v -> ble.writeCharacteristic("G_5".getBytes()));
        btn_pinch.setOnClickListener(v -> ble.writeCharacteristic("G_6".getBytes()));
    }

    // onConnectionStateChanged method from BluetoothConnectionListener interface
    @Override
    public void onConnectionStateChanged(boolean isConnected) {
        Intent onConnectionChangeIntent = new Intent(this, MainActivity.class);
        Log.d("MyApp", "Device connected or disconnected! Opening Menu");
        onConnectionChangeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(onConnectionChangeIntent);
    }

    // onDataReceive method to handle received BLE data
    @Override
    public void onDataReceive(int rxData) {
        Log.d("MyApp", "Data recieved: "+ rxData);
    }

}