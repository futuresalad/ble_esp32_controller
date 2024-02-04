package com.example.haendchen;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Arrays;

public class BLE {

    // Singleton instance for global access
    private static BLE instance = null;

    // Handler to post tasks on the main thread
    private final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));

    // Private constructor to enforce singleton pattern
    BLE(Context context) {
        this.context = context;
        initializeBluetooth();
    }

    // Static method to get singleton instance
    public static BLE getInstance(Context appContext) {
        if (instance == null) {
            instance = new BLE(appContext);
        }
        return instance;
    }

    // Member variables declaration
    private BluetoothConnectionListener listener;
    public void setBluetoothConnectionListener(BluetoothConnectionListener listener) {
        this.listener = listener;
    }

    public boolean isConnected() {
        return connected;
    }
    public boolean connected = false;
    private boolean servicesDiscovered = false;
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    public int connectionState = BluetoothProfile.STATE_DISCONNECTED;
    public int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private final Context context;
    private final List<ScanResult> scannedDevicesList = new ArrayList<>();
    private final ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    public BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private String deviceNameDesired = "dummyDeviceName";
    private BluetoothGatt connectedGatt;
    public BluetoothGattService nusservice;
    public BluetoothGattCharacteristic nusTxChar;
    public BluetoothGattCharacteristic nusRxChar;

    // UUIDs for Nordic UART Service (NUS)
    private final String nusUUID_str = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private final String nusTxCharUUID_str = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    private final String nusRxCharUUID_str = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    // Initialize Bluetooth manager and adapter
    void initializeBluetooth() {
        bluetoothManager = (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    // Start BLE scan with specified device name
    void startBleScan(String devicename) {
        deviceNameDesired = devicename;

        // Check location permission and start scanning
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Scanning for devices for 10 Seconds
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.startScan(null, scanSettings, scanCallback);
                handler.postDelayed(this::stopBleScan, 10000);
            }
        }
    }

    // Stop BLE scan
    void stopBleScan() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }
    }

    // ScanCallback to process discovered devices
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null) {

                Log.d("MyApp", "Entered Scancallback");

                if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    String deviceName = result.getDevice().getName();

                    if (deviceName != null) {

                        String currentAddress = result.getDevice().getAddress();

                        // As the devices keep on advertising, the list only gets filled if the device name is not already contained
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
                            Log.d("MyApp", "Found BLE device! Name: " + deviceName);
                            if (Objects.equals(result.getDevice().getName(), deviceNameDesired)) {
                                result.getDevice().connectGatt(context, true, gattCallback);
                            }
                        }
                    }
                }
            }
        }
    };


    // BluetoothGattCallback to handle connection changes and Bluetooth GATT operations
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            connectionState = newState;
            connectedGatt = gatt;

            String deviceAddress = connectedGatt.getDevice().getAddress();

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (connectionState == BluetoothProfile.STATE_CONNECTED) {

                        Log.w("MyApp", "Successfully connected to " + deviceAddress);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (connectedGatt != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                stopBleScan();
                                setConnected(true);
                                scannedDevicesList.clear();

                                if (!servicesDiscovered) {
                                    servicesDiscovered = connectedGatt.discoverServices();

                                }
                                if (listener != null) {
                                    listener.onConnectionStateChanged(true);
                                }
                            }
                        });
                    } else if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {

                        Log.w("MyApp", "Successfully disconnected from " + deviceAddress);
                        connectedGatt.close();
                        setConnected(false);
                        servicesDiscovered = false;

                        if (listener != null) {
                            listener.onConnectionStateChanged(false);
                        }
                    }

                } else {
                    Log.w("MyApp", "Error " + status + " encountered for " + deviceAddress + "! Disconnecting...");

                    connectedGatt.close();
                    setConnected(false);
                    servicesDiscovered = false;

                    if (listener != null) {
                        listener.onConnectionStateChanged(false);
                    }
                }
            } else {
                Log.e("MyApp", "Bluetooth Scan permission check failed");
            }
        }

        // Methods for GATT operations like reading/writing characteristics
        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] value, int success) {

            if (success == BluetoothGatt.GATT_SUCCESS) {
                Log.d("MyApp", "Value received: " + Arrays.toString(value));

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("MyApp", "Value written");
        }

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            Log.i("MyApp", "TX Phy: " + txPhy + "RX Phy: " + rxPhy + "Status: " + status);

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().toString().equals(nusUUID_str)) {

                        Log.i("MyApp", "Nus service discovered!");
                        nusservice = service;
                        nusTxChar = nusservice.getCharacteristic(UUID.fromString(nusTxCharUUID_str));
                        nusRxChar = nusservice.getCharacteristic(UUID.fromString(nusRxCharUUID_str));

                    } else {
                        Log.i("MyApp", "No such service");
                    }
                }
            }
        }
    };

    // Write to a specific BLE characteristic
    public void writeCharacteristic(byte[] data) {
        if (connectedGatt != null && nusRxChar != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            connectedGatt.writeCharacteristic(nusRxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }
    }
}