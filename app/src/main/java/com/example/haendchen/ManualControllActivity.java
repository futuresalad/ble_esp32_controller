package com.example.haendchen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ManualControllActivity extends AppCompatActivity implements BluetoothConnectionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_controll);

        BLE ble = BLE.getInstance(getApplicationContext());
        BLE.getInstance(this).setBluetoothConnectionListener(this);

        List<SeekBar> seekBars = new LinkedList<>(Arrays.asList(
                findViewById(R.id.seekBar_1),
                findViewById(R.id.seekBar_2),
                findViewById(R.id.seekBar_3),
                findViewById(R.id.seekBar_4),
                findViewById(R.id.seekBar_5)
        ));

        for (SeekBar seekBar : seekBars) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String value = seekBar.getContentDescription() + "_" + progress;
                    ble.writeCharacteristic(value.getBytes());

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Optional: Implement if needed
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }


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