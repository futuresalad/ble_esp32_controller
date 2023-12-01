package com.example.ble_esp32_controller;
import android.Manifest;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {
    private final List<ScanResult> items;
    private final OnClickListener onClickListener;
    private final Context context;

    public ScanResultAdapter(Context context, List<ScanResult> items, OnClickListener onClickListener) {
        this.context = context;
        this.items = items;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_scan_result, parent, false);
        return new ViewHolder(view, onClickListener, this.context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanResult item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final OnClickListener onClickListener;
        private final Context context;

        public ViewHolder(View itemView, OnClickListener onClickListener, Context context) {
            super(itemView);
            this.view = itemView;
            this.onClickListener = onClickListener;
            this.context = context;
        }
        public void bind(ScanResult result) {
            TextView deviceName = view.findViewById(R.id.device_name);
            TextView macAddress = view.findViewById(R.id.mac_address);
            TextView signalStrength = view.findViewById(R.id.signal_strength);

            if ((ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)) {
                deviceName.setText(result.getDevice().getName() != null ? result.getDevice().getName() : "Unnamed");
                macAddress.setText(result.getDevice().getAddress());
                signalStrength.setText(result.getRssi() + " dBm");

                view.setOnClickListener(v -> onClickListener.onClick(result));
            }
        }
    }

    public interface OnClickListener {
        void onClick(ScanResult device);
    }
}