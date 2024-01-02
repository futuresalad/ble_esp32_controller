package com.example.haendchen;

public interface BluetoothConnectionListener {
    void onConnectionStateChanged(boolean isConnected);
    void onDataReceive(int rxData);
}