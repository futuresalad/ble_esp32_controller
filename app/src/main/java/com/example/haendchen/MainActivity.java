package com.example.haendchen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements BluetoothConnectionListener {

    boolean permissions_granted = false;
    boolean device_connected = false;
    BLE ble;
    Button btnConnectDevice;
    Button btnManualControl;
    Button btnGripPatterns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ble = BLE.getInstance(getApplicationContext());
        BLE.getInstance(this).setBluetoothConnectionListener(this);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {
                finish();
            }};

        getOnBackPressedDispatcher().addCallback(this, callback);

         btnConnectDevice = findViewById(R.id.connect_device_btn);
         btnManualControl = findViewById(R.id.manual_control_btn);
         btnGripPatterns = findViewById(R.id.grip_patterns_btn);

        updateUIBasedOnConnectionStatus();

        btnConnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAndRequestPermissions()) {
                    if (ble.bluetoothAdapter != null && !ble.bluetoothAdapter.isEnabled()) {

                        Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }

                        startActivity(enableBleIntent);

                    } else {
                        Intent scanResultIntent = new Intent(MainActivity.this, ConnectDeviceActivity.class);
                        startActivity(scanResultIntent);
                    }
                }
            }

        });

        btnManualControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent manualControlIntent = new Intent(MainActivity.this, ManualControllActivity.class);
                startActivity(manualControlIntent);

            }
        });

        btnGripPatterns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gripPatternsIntent = new Intent(MainActivity.this, GripPatternsActivity.class);
                startActivity(gripPatternsIntent);

            }
        });

    }

    private void updateUIBasedOnConnectionStatus() {

        if (BLE.getInstance(this).isConnected()) {

            btnConnectDevice.setVisibility(View.GONE);
            btnManualControl.setVisibility(View.VISIBLE);
            btnGripPatterns.setVisibility(View.VISIBLE);

        } else {
            btnConnectDevice.setVisibility(View.VISIBLE);
            btnManualControl.setVisibility(View.GONE);
            btnGripPatterns.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        permissions_granted = checkAndRequestPermissions();
        updateUIBasedOnConnectionStatus();
    }

    private boolean checkAndRequestPermissions() {

        boolean ret = true;

        // Linked list for faster access to unique items
        LinkedList<String> permissions = new LinkedList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.BLUETOOTH_SCAN);
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(permission);
                ret = false;
            }
        }
        return ret;
    }

    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showPermissionRationale(permission);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, ble.ENABLE_BLUETOOTH_REQUEST_CODE);
        }
    }

    private void showPermissionRationale(String permission) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Permission for Bluetooth required")
                .setTitle("Permission Required")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) ->
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, ble.ENABLE_BLUETOOTH_REQUEST_CODE))

                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void checkAndEnableBluetooth() {

            Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                startActivity(enableBleIntent);
            }
    }

    @Override
    public void onConnectionStateChanged(boolean isConnected) {
        if (isConnected) {
            Log.d("MyApp", "Device connected! Menu available");

        }
        else {
            Log.d("MyApp", "Device disconnected! Returning");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUIBasedOnConnectionStatus();
            }
        });
    }

    @Override
    public void onDataReceive(int rxData) {
        Log.d("MyApp", "Data recieved: "+ rxData);
    }

}