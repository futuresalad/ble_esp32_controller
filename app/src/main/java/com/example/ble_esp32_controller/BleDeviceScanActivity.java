package com.example.ble_esp32_controller;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.util.LinkedList;
import java.util.Objects;


public class BleDeviceScanActivity extends AppCompatActivity {

    final private int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private boolean isScanning = false;
    private static final long SCAN_PERIOD = 10000;
    private final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private final String deviceName = "zEPHYr";
    //private final ScanFilter filter = new ScanFilter.Builder().setDeviceName(deviceName).build();
    private final ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            super.onScanResult(callbackType, result);
            Log.d("Scancallback", ("Entered Scancallback"));

            if (result != null) {
                Context context = BleDeviceScanActivity.this;

                if (!(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_device_scan);

        initializeBluetooth();


    }

    @Override
    protected void onResume() {
        super.onResume();

        checkAndRequestPermissions();
        checkAndEnableBluetooth();
    }

    private void startBleScan(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.startScan(null, scanSettings, scanCallback);
            isScanning = true;
        }
    }

    private void stopBleScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.stopScan(scanCallback);
            isScanning = false;
        }
    }

    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

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
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(permission);
            }
        }
    }
    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showPermissionRationale(permission);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, ENABLE_BLUETOOTH_REQUEST_CODE);
        }
    }
    private void showPermissionRationale(String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Permission for Bluetooth required")
                .setTitle("Permission Required")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) ->
                        ActivityCompat.requestPermissions(BleDeviceScanActivity.this, new String[]{permission}, ENABLE_BLUETOOTH_REQUEST_CODE))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void checkAndEnableBluetooth() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                startActivity(enableBleIntent);
            }
        }
    }
}
