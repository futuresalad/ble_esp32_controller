package com.example.ble_esp32_controller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.LinkedList;
import java.util.Objects;

public class BLE {

    final  int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private final Context context;
    boolean isScanning = false;
    final long SCAN_PERIOD = 10000;
    final String deviceName = "zEPHYr";
    final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    //private final ScanFilter filter = new ScanFilter.Builder().setDeviceName(deviceName).build();
    final ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

    ActivityCompat activity;

    public BLE(Context context, ActivityCompat activity) {
        this.context = context;
        this.activity = activity;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            super.onScanResult(callbackType, result);
            Log.d("Scancallback", ("Entered Scancallback"));

            if (result != null) {


                if (!(context.checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    String deviceName = result.getDevice().getName();

                    if (deviceName != null) {
                        Log.d("Scancallback", ("Found BLE device! Name: %s " + deviceName));

                    } else {
                        Log.d("Scancallback", ("Found BLE device! Unnamed"));
                    }
                }
            }

        }
    };

    private void startBleScan(){
        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.startScan(null, scanSettings, scanCallback);
            isScanning = true;
        }
    }

    private void stopBleScan() {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.stopScan(scanCallback);
            isScanning = false;
        }
    }

    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            Log.e("Bluetooth", "BluetoothManager initialization failed");
            return;
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e("Bluetooth", "BluetoothAdapter initialization failed");
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            Log.e("Bluetooth", "BluetoothLeScanner initialization failed");
            return;
        }

// Finish setup
    }

    private void checkAndRequestPermissions() {

        // Linked list for faster access to unique items
        LinkedList<String> permissions = new LinkedList<>();
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        permissions.add(Manifest.permission.BLUETOOTH_SCAN);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                //requestPermission(permission);
            }
        }
    }
}

