package com.example.ble_esp32_controller;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Objects;


public class BleDeviceScanActivity extends AppCompatActivity {
    final private int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private boolean scanning;
    private static final long SCAN_PERIOD = 10000;
    private final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_device_scan);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = (android.bluetooth.BluetoothAdapter) bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.BLUETOOTH_CONNECT);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
        }
    }

    private void promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission to use Bluetooth is granted", Toast.LENGTH_LONG).show();

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.BLUETOOTH_CONNECT)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission for Bluetooth required")
                        .setTitle("Permission Required")
                        .setCancelable(false)
                        .setPositiveButton("Aight", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(BleDeviceScanActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, ENABLE_BLUETOOTH_REQUEST_CODE);
                                    }
                                }
                        )
                        .setNegativeButton("Cancel", ((dialog, which)->dialog.dismiss()));
                builder.show();
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, ENABLE_BLUETOOTH_REQUEST_CODE);
            }
            startActivity(i);
        }
        return;
    }
}


