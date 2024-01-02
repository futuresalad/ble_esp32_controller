package com.example.haendchen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;


public class GripPatternsActivity extends AppCompatActivity implements BluetoothConnectionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grip_patterns);

        BLE ble = BLE.getInstance(getApplicationContext());
        BLE.getInstance(this).setBluetoothConnectionListener(this);


        Button btn_open = findViewById(R.id.btn_open);
        Button btn_grab = findViewById(R.id.btn_grab);
        Button btn_indicate = findViewById(R.id.btn_indicate);
        Button btn_thumbs = findViewById(R.id.btn_thumbs);
        Button btn_rock = findViewById(R.id.btn_rock);
        Button btn_pinch = findViewById(R.id.btn_pinch);

        btn_open.setOnClickListener(v -> ble.writeCharacteristic("G_1".getBytes()));
        btn_grab.setOnClickListener(v -> ble.writeCharacteristic("G_2".getBytes()));
        btn_indicate.setOnClickListener(v -> ble.writeCharacteristic("G_3".getBytes()));
        btn_thumbs.setOnClickListener(v -> ble.writeCharacteristic("G_4".getBytes()));
        btn_rock.setOnClickListener(v -> ble.writeCharacteristic("G_5".getBytes()));
        btn_pinch.setOnClickListener(v -> ble.writeCharacteristic("G_6".getBytes()));

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