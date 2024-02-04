package com.example.haendchen;


// Interface for communicating BLE connection changes throughout the app
public interface BluetoothConnectionListener {
    void onConnectionStateChanged(boolean isConnected);
    void onDataReceive(int rxData);
}