package com.example.ble_esp32_controller;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class BleDeviceScanActivity extends AppCompatActivity {

    public boolean isBluetoothenabled() {
        return bluetoothenabled;
    }

    public void setBluetoothenabled(boolean bluetoothenabled) {
        this.bluetoothenabled = bluetoothenabled;
    }

    List<ScanResult> scannedDevicesList = new ArrayList<>();
    final private int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private boolean bluetoothenabled = false;
    private BluetoothLeScanner bluetoothLeScanner;
    private final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private BluetoothAdapter bluetoothAdapter;
    private ScanResultAdapter scanResultAdapter = new ScanResultAdapter(this, scannedDevicesList, new ScanResultAdapter.OnClickListener() {
        @Override
        public void onClick(ScanResult device) {
            Log.d("Connecting", "Attempting connection");
        }
    });

    private final String deviceNameDesired = "zEPHYr";
    //private final ScanFilter filter = new ScanFilter.Builder().setDeviceName(deviceName).build();
    private final ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null) {
                Context context = BleDeviceScanActivity.this;
                Log.d("Scancallback", "Entered Scancallback");

                if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    String deviceName = result.getDevice().getName();

                    if (deviceName != null) {
                        Log.d("Scancallback", "Found BLE device! Name: " + deviceName);
                        String currentAddress = result.getDevice().getAddress();
                        boolean isDuplicate = false;

                        for (ScanResult scannedResult : scannedDevicesList) {
                            String scannedAddress = scannedResult.getDevice().getAddress();
                            if (currentAddress != null && currentAddress.equals(scannedAddress)) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (!isDuplicate) {
                            scannedDevicesList.add(result);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scanResultAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }
        }
    };
    private void setupRecyclerView(ScanResultAdapter scanResultAdapter) {
        RecyclerView recyclerView = findViewById(R.id.scan_result_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(scanResultAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_device_scan);

        RecyclerView scanResultRecyclerView = findViewById(R.id.scan_result_recycler_view);


        setupRecyclerView(scanResultAdapter);

        initializeBluetooth();
        Button btnScan = findViewById(R.id.startScan);

        if(!bluetoothAdapter.isEnabled()) {
            btnScan.setClickable(false);
        }

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBleScan();
                btnScan.setText("Scanning...");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopBleScan();
                        btnScan.setText("Scan for devices");
                    }
                }, 5000); // 5000 milliseconds = 5 seconds
            }

        }
        );
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
        }
    }

    private void stopBleScan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.stopScan(scanCallback);
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
