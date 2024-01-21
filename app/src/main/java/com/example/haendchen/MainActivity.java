package com.example.haendchen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.LinkedList;


public class MainActivity extends AppCompatActivity implements BluetoothConnectionListener {
    boolean permissions_granted = false;
    boolean device_connected = false;
    BLE ble;
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    SwitchMaterial switchNightMode;
    Button btnConnectDevice;
    Button btnManualControl;
    Button btnGripPatterns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ble = BLE.getInstance(getApplicationContext());
        BLE.getInstance(this).setBluetoothConnectionListener(this);

        OnBackPressedCallback callbackMainBack = new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {
                finish();
            }};

        getOnBackPressedDispatcher().addCallback(this, callbackMainBack);

        SwitchCompat switchNightMode = findViewById(R.id.dark_light_switch);
        sharedPreferences = getSharedPreferences("MODE", MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode", false);

        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", false);



            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", true);

            }
            editor.apply();
        });

        btnConnectDevice = findViewById(R.id.connect_device_btn);
        btnManualControl = findViewById(R.id.manual_control_btn);
        btnGripPatterns = findViewById(R.id.grip_patterns_btn);

        updateUIBasedOnConnectionStatus();

        btnConnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setCaptureActivity(CaptureActivity.class);
                integrator.setOrientationLocked(true);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("Scan QR Code on device");
                integrator.initiateScan();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                if (checkAndRequestPermissions()) {
                    if (ble.bluetoothAdapter != null && !ble.bluetoothAdapter.isEnabled()) {
                        Intent enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        startActivity(enableBleIntent);
                    } else {
                        Intent scanResultIntent = new Intent(MainActivity.this, ConnectDeviceActivity.class);
                        scanResultIntent.putExtra("QR_CODE_CONTENT", result.getContents()); // Pass the QR code content
                        startActivity(scanResultIntent);
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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